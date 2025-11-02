package com.sinloingok.app.util.ns;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.service.order.CommandExecutor;
import com.sinloingok.app.util.Trace;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class PulseSignalProcessor {

    private final NettyChannelRegistry channelRegistry;

    public void process(ChannelHandlerContext ctx, String hexData, Supplier<CommandExecutor> executorSupplier) {
        if (hexData.length() < 16) {
            log.warn("trace={} phase=ingress step=validate msg=switch_hex_too_short len={} hex={}",
                    MDC.get("trace"), hexData.length(), hexData);
            return;
        }

        String onlyCode = channelRegistry.getOnlyCode(ctx.channel());
        String beginSignal;
        String endSignal;

        if (onlyCode != null && NetSiteCache.pmByOnlyCode(onlyCode) != null) {
            beginSignal = SignalPayloadUtils.safeSub(hexData, 12, 14);
            endSignal = SignalPayloadUtils.safeSub(hexData, 14, 16);
        } else {
            beginSignal = SignalPayloadUtils.reverseBitsLookup(SignalPayloadUtils.safeSub(hexData, 12, 14));
            endSignal = SignalPayloadUtils.reverseBitsLookup(SignalPayloadUtils.safeSub(hexData, 14, 16));
        }

        if (onlyCode == null) {
            log.warn("trace={} phase=ingress step=bind msg=onlyCode_not_found hex={} begin={} end={}",
                    MDC.get("trace"), hexData, beginSignal, endSignal);
            return;
        }

        handlePulseSignal(onlyCode, beginSignal, endSignal, executorSupplier);
    }

    private void handlePulseSignal(String onlyCode, String beginSignal, String endSignal,
                                   Supplier<CommandExecutor> executorSupplier) {
        long t0 = System.nanoTime();

        DeviceControl.Action action = "00".equals(beginSignal) ? DeviceControl.Action.CLOSE : DeviceControl.Action.OPEN;
        int b = safeConvert(beginSignal);
        int e = safeConvert(endSignal);
        int channelNo = b + e;

        String funcCode = channelNo > 0 ? SignalTopology.getFunctionCode(channelNo) : "NA";
        String funcName = channelNo > 0 ? SignalTopology.getFunctionName(channelNo) : "NA";

        String traceId = Trace.newId(onlyCode);
        Trace.bind(traceId, onlyCode, channelNo, action.name(), funcCode, funcName);

        try {
            boolean runnable = (NetSiteCache.wscByOnlyCode(onlyCode) != null
                    && NetSiteStatus.USING.equals(NetSiteCache.wscByOnlyCode(onlyCode).getStatus()))
                    || (NetSiteCache.pmByOnlyCode(onlyCode) != null);

            log.info("trace={} phase=FLOW_START funcName={} funcCode={} action={} channel={} onlyCode={} begin={} end={}",
                    traceId, funcName, funcCode, action.name(), channelNo, onlyCode, beginSignal, endSignal);

            if (!runnable) {
                log.info("trace={} phase=gateway step=guard msg=site_not_running", traceId);
                return;
            }

            log.info("trace={} phase=gateway step=dispatch to=CommandExecutor", traceId);
            CommandExecutor executor = executorSupplier.get();
            boolean ok = executor != null && executor.executeWithTrace(onlyCode, channelNo, action, traceId);
            long costMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("trace={} phase=gateway step=done result={} costMs={}", traceId, ok ? "OK" : "FAIL", costMs);
        } catch (Exception e1) {
            long costMs = (System.nanoTime() - t0) / 1_000_000;
            log.error("trace={} phase=gateway step=exception costMs={}", traceId, costMs, e1);
        } finally {
            Trace.clear();
        }
    }

    private int safeConvert(String hex) {
        try {
            return SignalPayloadUtils.convert(hex);
        } catch (Exception e) {
            log.warn("trace={} phase=gateway step=convert_fail hex={}", MDC.get("trace"), hex);
            return 0;
        }
    }
}
