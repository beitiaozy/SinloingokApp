package com.sinloingok.app.service.order;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.models.Command;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.voice.OperationVoiceNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandExecutor {

    private final DeviceCommandSender deviceSender;       // 硬件下发
    private final SettlementService settlementService;    // 结算
    private final OperationVoiceNotifier operationVoiceNotifier;

    /** 供 HandlerServer 传入现成 traceId 的入口 */
    public boolean executeWithTrace(String onlyCode, int chNum, DeviceControl.Action commandStr, String traceId) {
        String funcCode = chNum > 0 ? SignalTopology.getFunctionCode(chNum) : "NA";
        String funcName = chNum > 0 ? SignalTopology.getFunctionName(chNum) : "NA";
        com.sinloingok.app.util.Trace.bind(traceId, onlyCode, chNum, commandStr.name(), funcCode, funcName);
        try {
            return execute(onlyCode, chNum, commandStr);
        } finally {
            // 由 HandlerServer 统一 clear
        }
    }

    /**
     * 入口：输入 (onlyCode, chNum, commandStr)，内部解析 → 执行控制/结算
     */
    public boolean execute(String onlyCode, int chNum, DeviceControl.Action commandStr) {
        long tExec0 = System.nanoTime();
        CommandResolver.DualCommand pair = CommandResolver.resolve(onlyCode, chNum, commandStr);

        // 1) 控制：有 control 才执行
        if (pair.getControl() != null) {
            Command c = pair.getControl();
            long tCtrl0 = System.nanoTime();
            try {
                boolean ok = sendCommand(c);
                long costMs = (System.nanoTime() - tCtrl0) / 1_000_000;
                log.info("trace={} phase=control step=send target={}:{} cmd={} result={} costMs={}",
                        MDC.get("trace"), c.getOnlyCode(), c.getChannel(), c.getCommand(), ok ? "OK" : "FAIL", costMs);
                if (!ok) return false;

                notifyVoice(c);

                // ★ 仅在控制成功后更新状态（本机或 PMKZSB 目标）
                DeviceControl dc = NetSiteCache.wscDeviceControl(onlyCode);
                if (dc != null && c.getOnlyCode().equals(onlyCode)) {
                    if (chNum == 3) {
                        dc.updateState(3, c.getCommand());
                        log.info("trace={} phase=state step=update sourceCh=3 writeCh=3 newState={}",
                                MDC.get("trace"), c.getCommand());
                    } else if (chNum == 8) {
                        dc.updateState(8, c.getCommand());
                        dc.updateState(3, c.getCommand());
                        log.info("trace={} phase=state step=update sourceCh=8 writeCh=8,3 mirror=true newState={}",
                                MDC.get("trace"), c.getCommand());
                    } else {
                        dc.updateState(c.getChannel(), c.getCommand());
                        log.info("trace={} phase=state step=update sourceCh={} writeCh={} newState={}",
                                MDC.get("trace"), chNum, c.getChannel(), c.getCommand());
                    }
                    NetSiteCache.refreshDeviceControl(onlyCode, dc);
                }
            } catch (Exception e) {
                log.error("trace={} phase=control step=exception", MDC.get("trace"), e);
                return false;
            }
        }

        // 2) 结算：有 settle 才执行
        if (pair.getSettle() != null) {
            Command s = pair.getSettle();
            long tSet0 = System.nanoTime();
            try {
                if (s.getChannel() == -1 &&  DeviceControl.Action.OVER.equals(s.getCommand())) {
                    log.info("trace={} phase=settle step=start type=ALL", MDC.get("trace"));
                    settlementService.settleAll(s.getOnlyCode());
                } else {
                    log.info("trace={} phase=settle step=start type=CHANNEL targetCh={} cmd={}",
                            MDC.get("trace"), s.getChannel(), s.getCommand());
                    settlementService.settleChannel(s.getOnlyCode(), s.getChannel(), s.getCommand());
                }
                long costMs = (System.nanoTime() - tSet0) / 1_000_000;
                log.info("trace={} phase=settle step=done costMs={}", MDC.get("trace"), costMs);
                notifyVoice(s);
            } catch (Exception e) {
                log.error("trace={} phase=settle step=exception", MDC.get("trace"), e);
                return false;
            }
        }

        long execMs = (System.nanoTime() - tExec0) / 1_000_000;
        log.info("trace={} phase=executor step=finish result=OK costMs={}", MDC.get("trace"), execMs);
        return true;
    }

    private boolean sendCommand(Command command){
        return deviceSender.send(command.getOnlyCode(), command.getChannel(), command.getCommand());
    }

    private void notifyVoice(Command command) {
        if (command == null) {
            return;
        }
        if (command.getChannel() == -1 && DeviceControl.Action.OVER.equals(command.getCommand())) {
            operationVoiceNotifier.onSettlement(command.getOnlyCode());
            return;
        }
        operationVoiceNotifier.onChannelEvent(command.getOnlyCode(), command.getChannel(), command.getCommand());
    }

    public void turnOnPmNetSiteDevice(String onlyCode){
        Command c = CommandResolver.pmOpenCommand(onlyCode);
        sendCommand(c);
    }

    public void turnOffPmNetSiteDevice(String onlyCode){
        Command c = CommandResolver.pmCloseCommand(onlyCode);
        sendCommand(c);
    }

    // 空压机联动（保持不变，可按需也加 trace）
    private final String kyOnlyCode = "0090B20057B9";
    private volatile boolean airCompressorOff = true;

    public void turnOnAirCompressorPower() {
        deviceSender.open(kyOnlyCode, 1);
        airCompressorOff = false;
        log.info("trace={} phase=ky step=on", MDC.get("trace"));
    }

    public void turnOffAirCompressPower() {
        if (!airCompressorOff) {
            log.info("trace={} phase=ky step=off", MDC.get("trace"));
            deviceSender.close(kyOnlyCode, 1);
            airCompressorOff = true;
        }
    }
}
