package com.sinloingok.app.util.ns;

import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.order.CommandExecutor;
import com.sinloingok.app.util.ns.DeviceConnectionTracker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatProcessor {

    private final NettyChannelRegistry channelRegistry;
    private final CommandExecutor commandExecutor;

    public String process(ChannelHandlerContext ctx, String hexData, Supplier<NetSiteService> netSiteServiceSupplier) {
        if (hexData.length() < 34) {
            log.warn("trace={} phase=heartbeat step=validate msg=heartbeat_hex_too_short len={} hex={}",
                    MDC.get("trace"), hexData.length(), hexData);
            return null;
        }
        String onlyCode = extractOnlyCodeFromHeartbeat(hexData);
        onlyCode = onlyCodeChange(onlyCode);

        Channel oldChannel = channelRegistry.register(onlyCode, ctx.channel());
        logChannelUpdate(onlyCode, ctx.channel(), oldChannel);

        DeviceConnectionTracker.markOnline(onlyCode);
        if (isNewOrReplacedChannel(ctx.channel(), oldChannel)) {
            commandExecutor.replayPending(onlyCode);
        }

        channelRegistry.sendMsg(onlyCode, hexData);
        handleHeartbeat(onlyCode, netSiteServiceSupplier);
        return onlyCode;
    }

    private void handleHeartbeat(String onlyCode, Supplier<NetSiteService> netSiteServiceSupplier) {
        try {
            NetSiteService service = netSiteServiceSupplier.get();
            if (service != null) {
                service.registerOrRefreshNetSite(onlyCode);
            }
        } catch (Exception e) {
            log.error("trace={} phase=heartbeat step=handle_exception onlyCode={}", MDC.get("trace"), onlyCode, e);
        }
    }

    private String extractOnlyCodeFromHeartbeat(String hexData) {
        String dataPart = hexData.substring(10, 34);
        return IntStream.range(0, 12)
                .mapToObj(i -> dataPart.substring(i * 2, i * 2 + 2))
                .map(this::hexToAscii)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String hexToAscii(String hex) {
        int decimal = Integer.parseInt(hex, 16);
        return String.valueOf((char) decimal);
    }

    private String onlyCodeChange(String onlyCode){
        if("78EE4C6C8DFC".equals(onlyCode)){
            onlyCode = "0090D500242A";
        }
        if("9015060247C4".equals(onlyCode)){
            onlyCode = "0090D5000F10";
        }
        if("90150604FDDC".equals(onlyCode)){
            onlyCode = "0090D500245B";
        }
        return onlyCode;
    }

    private void logChannelUpdate(String onlyCode, Channel newChannel, Channel oldChannel) {
        if (oldChannel == null) {
            log.info("trace={} phase=heartbeat step=register onlyCode={} channelId={}",
                    MDC.get("trace"), onlyCode, newChannel.id().asLongText());
        } else if (!oldChannel.id().equals(newChannel.id())) {
            log.warn("trace={} phase=heartbeat step=replace onlyCode={} oldChannelId={} newChannelId={}",
                    MDC.get("trace"), onlyCode, oldChannel.id().asLongText(), newChannel.id().asLongText());
        } else {
            log.debug("trace={} phase=heartbeat step=refresh onlyCode={} channelId={}",
                    MDC.get("trace"), onlyCode, newChannel.id().asLongText());
        }
    }

    private boolean isNewOrReplacedChannel(Channel current, Channel oldChannel) {
        if (oldChannel == null) {
            return true;
        }
        return !oldChannel.id().equals(current.id());
    }
}
