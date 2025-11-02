package com.sinloingok.app.service.order;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.models.Command;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandExecutor {

    private final DeviceCommandSender deviceSender;       // 硬件下发
    private final SettlementService settlementService;    // 结算
    private final ChannelLockManager channelLockManager;  // 通道锁

    private final ConcurrentHashMap<String, PendingInstruction> pendingInstructions = new ConcurrentHashMap<>();

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

        Command controlCommand = pair.getControl();
        Command settleCommand = pair.getSettle();

        logExecutionPlan(onlyCode, chNum, commandStr, controlCommand, settleCommand);

        StepOutcome controlOutcome = StepOutcome.SKIPPED;
        String controlDetail = controlCommand == null ? "no_control" : describeCommand(controlCommand);
        ChannelLockManager.AcquireResult lockResult = ChannelLockManager.AcquireResult.NOT_REQUIRED;
        boolean shouldExecuteSettlement = true;

        if (controlCommand != null) {
            long tCtrl0 = System.nanoTime();
            try {
                lockResult = channelLockManager.acquire(controlCommand);
                if (ChannelLockManager.AcquireResult.BUSY.equals(lockResult)) {
                    controlOutcome = StepOutcome.FAILED;
                    controlDetail = "channel_locked";
                    shouldExecuteSettlement = false;
                } else {
                    DeviceCommandSender.SendResult sendResult = sendCommand(controlCommand);
                    boolean ok = sendResult.isSuccess();
                    long costMs = (System.nanoTime() - tCtrl0) / 1_000_000;
                    log.debug("trace={} phase=control step=send target={}:{} cmd={} result={} costMs={}",
                            MDC.get("trace"), controlCommand.getOnlyCode(), controlCommand.getChannel(),
                            controlCommand.getCommand(), ok ? "OK" : "FAIL", costMs);
                    if (ok) {
                        clearPending(onlyCode, controlCommand);
                        updateLocalState(onlyCode, chNum, controlCommand);
                        controlOutcome = StepOutcome.SUCCESS;
                        controlDetail = formatSuccessDetail(controlCommand, costMs);
                    } else {
                        controlOutcome = StepOutcome.FAILED;
                        controlDetail = describeSendFailure(sendResult);
                        shouldExecuteSettlement = false;
                        rememberPendingIfOffline(onlyCode, chNum, commandStr, controlCommand, sendResult);
                        if (ChannelLockManager.AcquireResult.ACQUIRED.equals(lockResult)) {
                            channelLockManager.release(controlCommand);
                        }
                        if(chNum == 6){
                            shouldExecuteSettlement = true;
                        }
                    }
                }
            } catch (Exception e) {
                controlOutcome = StepOutcome.FAILED;
                controlDetail = "exception:" + e.getMessage();
                shouldExecuteSettlement = false;
                log.error("trace={} phase=control step=exception", MDC.get("trace"), e);
                if (ChannelLockManager.AcquireResult.ACQUIRED.equals(lockResult)) {
                    channelLockManager.release(controlCommand);
                }
            }
        }

        SettlementService.SettlementResult settlementResult = SettlementService.SettlementResult.skipped("no_settle");
        if (shouldExecuteSettlement && settleCommand != null) {
            long tSet0 = System.nanoTime();
            try {
                if (settleCommand.getChannel() == -1 && DeviceControl.Action.OVER.equals(settleCommand.getCommand())) {
                    settlementResult = settlementService.settleAll(settleCommand.getOnlyCode());
                } else {
                    settlementResult = settlementService.settleChannel(
                            settleCommand.getOnlyCode(), settleCommand.getChannel(), settleCommand.getCommand());
                }
                if (settlementResult.isExecuted() && settlementResult.getCostMs() == 0L) {
                    long costMs = (System.nanoTime() - tSet0) / 1_000_000;
                    settlementResult = SettlementService.SettlementResult.success(settlementResult.getDetail(), costMs);
                }
            } catch (Exception e) {
                log.error("trace={} phase=settle step=exception", MDC.get("trace"), e);
                settlementResult = SettlementService.SettlementResult.failed("exception:" + e.getMessage(),
                        (System.nanoTime() - tSet0) / 1_000_000);
            }
        } else if (!shouldExecuteSettlement && settleCommand != null) {
            settlementResult = SettlementService.SettlementResult.skipped("control_failed");
        }

        long execMs = (System.nanoTime() - tExec0) / 1_000_000;
        boolean overallSuccess = StepOutcome.SUCCESS.equals(controlOutcome) || controlCommand == null;
        if (settleCommand != null) {
            overallSuccess = overallSuccess && settlementResult.isSuccess();
        }

        logExecutionSummary(onlyCode, chNum, commandStr, controlCommand, controlOutcome, controlDetail,
                settleCommand, settlementResult, execMs);

        return overallSuccess;
    }

    private DeviceCommandSender.SendResult sendCommand(Command command){
        return deviceSender.send(command.getOnlyCode(), command.getChannel(), command.getCommand());
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
        log.debug("trace={} phase=ky step=on", MDC.get("trace"));
    }

    public void turnOffAirCompressPower() {
        if (!airCompressorOff) {
            log.debug("trace={} phase=ky step=off", MDC.get("trace"));
            deviceSender.close(kyOnlyCode, 1);
            airCompressorOff = true;
        }
    }

    public void replayPending(String onlyCode) {
        PendingInstruction pending = pendingInstructions.get(onlyCode);
        if (pending == null) {
            log.debug("trace={} phase=control step=pending_check onlyCode={} result=none", MDC.get("trace"), onlyCode);
            return;
        }
        log.info("trace={} phase=control step=replay_pending onlyCode={} channel={} action={} reason={}",
                MDC.get("trace"), pending.onlyCode, pending.channel, pending.action, pending.reason);
        execute(pending.onlyCode, pending.channel, pending.action);
    }

    private void rememberPendingIfOffline(String onlyCode, int chNum, DeviceControl.Action commandStr,
                                          Command controlCommand, DeviceCommandSender.SendResult sendResult) {
        if (sendResult == null || !sendResult.isOffline() || controlCommand == null) {
            return;
        }
        PendingInstruction pending = new PendingInstruction(onlyCode, chNum, commandStr,
                controlCommand.getOnlyCode(), controlCommand.getChannel(), sendResult.getDetail());
        pendingInstructions.put(onlyCode, pending);
        log.warn("trace={} phase=control step=pending_store onlyCode={} channel={} action={} detail={}",
                MDC.get("trace"), onlyCode, chNum, commandStr, sendResult.getDetail());
    }

    private void clearPending(String onlyCode, Command controlCommand) {
        if (controlCommand == null) {
            return;
        }
        PendingInstruction existing = pendingInstructions.get(onlyCode);
        if (existing != null && Objects.equals(existing.controlOnlyCode, controlCommand.getOnlyCode())
                && existing.controlChannel == controlCommand.getChannel()) {
            pendingInstructions.remove(onlyCode);
            log.debug("trace={} phase=control step=pending_clear onlyCode={} channel={}",
                    MDC.get("trace"), onlyCode, controlCommand.getChannel());
        }
    }

    private String describeSendFailure(DeviceCommandSender.SendResult sendResult) {
        if (sendResult == null) {
            return "send_failed";
        }
        return String.format("%s:%s", sendResult.getStatus().name(), sendResult.getDetail());
    }

    private void logExecutionPlan(String onlyCode, int chNum, DeviceControl.Action commandStr,
                                  Command controlCommand, Command settleCommand) {
        String funcCode = chNum > 0 ? safeFuncCode(chNum) : "NA";
        String funcName = chNum > 0 ? safeFuncName(chNum) : describeChannel(chNum);
        log.info("trace={} 指令開始 -> 設備={} 通道={}({}) 功能={} 動作={}({}) 控制={} 結算={}",
                MDC.get("trace"), onlyCode, chNum, funcCode, funcName, commandStr,
                actionMeaning(commandStr),
                controlCommand == null ? "無" : describeCommand(controlCommand),
                settleCommand == null ? "無" : describeCommand(settleCommand));
    }

    private void logExecutionSummary(String onlyCode, int chNum, DeviceControl.Action originalCommand,
                                     Command controlCommand, StepOutcome controlOutcome, String controlDetail,
                                     Command settleCommand, SettlementService.SettlementResult settlementResult,
                                     long execCostMs) {
        String controlSummary;
        if (controlCommand == null) {
            controlSummary = "無控制";
        } else {
            controlSummary = String.format("%s(%s)", controlOutcome.getLabel(), controlDetail);
        }

        String settleSummary;
        if (settleCommand == null) {
            settleSummary = "無計費";
        } else if (settlementResult.isExecuted()) {
            settleSummary = String.format("%s(%s, cost=%dms)",
                    settlementResult.isSuccess() ? "成功" : "失敗", settlementResult.getDetail(),
                    settlementResult.getCostMs());
        } else {
            settleSummary = String.format("跳過(%s)", settlementResult.getDetail());
        }

        log.info("trace={} 指令完成 -> 設備={} 通道={} 動作={}({}) 控制結果={} 計費結果={} 耗時={}ms",
                MDC.get("trace"), onlyCode, chNum, originalCommand, actionMeaning(originalCommand),
                controlSummary, settleSummary, execCostMs);
    }

    private enum StepOutcome {
        SUCCESS("成功"),
        FAILED("失敗"),
        SKIPPED("跳過");

        private final String label;

        StepOutcome(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private void updateLocalState(String onlyCode, int sourceChannel, Command controlCommand) {
        DeviceControl dc = NetSiteCache.wscDeviceControl(onlyCode);
        if (dc == null || !controlCommand.getOnlyCode().equals(onlyCode)) {
            return;
        }

        if (sourceChannel == 3) {
            dc.updateState(3, controlCommand.getCommand());
            log.debug("trace={} phase=state step=update sourceCh=3 writeCh=3 newState={}",
                    MDC.get("trace"), controlCommand.getCommand());
        } else if (sourceChannel == 8) {
            dc.updateState(8, controlCommand.getCommand());
            dc.updateState(3, controlCommand.getCommand());
            log.debug("trace={} phase=state step=update sourceCh=8 writeCh=8,3 mirror=true newState={}",
                    MDC.get("trace"), controlCommand.getCommand());
        } else {
            dc.updateState(controlCommand.getChannel(), controlCommand.getCommand());
            log.debug("trace={} phase=state step=update sourceCh={} writeCh={} newState={}",
                    MDC.get("trace"), sourceChannel, controlCommand.getChannel(), controlCommand.getCommand());
        }
        NetSiteCache.refreshDeviceControl(onlyCode, dc);
    }

    private String describeCommand(Command command) {
        String channelDesc = describeChannel(command.getChannel());
        return String.format("%s:%s %s(%s) %s", command.getOnlyCode(), channelDesc,
                safeFuncName(command.getChannel()), safeFuncCode(command.getChannel()),
                actionMeaning(command.getCommand()));
    }

    private String describeChannel(int channel) {
        if (channel == -1) {
            return "ALL";
        }
        if (channel == 0) {
            return "GLOBAL";
        }
        return String.valueOf(channel);
    }

    private String safeFuncName(int channel) {
        if (channel <= 0) {
            return channel == 0 ? "總控" : "全部";
        }
        try {
            return SignalTopology.getFunctionName(channel);
        } catch (Exception e) {
            return "CH" + channel;
        }
    }

    private String safeFuncCode(int channel) {
        if (channel <= 0) {
            return channel == 0 ? "CTRL" : "ALL";
        }
        try {
            return SignalTopology.getFunctionCode(channel);
        } catch (Exception e) {
            return "CH" + channel;
        }
    }

    private String actionMeaning(DeviceControl.Action action) {
        switch (action) {
            case OPEN:
                return "開啟";
            case CLOSE:
                return "關閉";
            case OVER:
                return "結束";
            case READ:
                return "讀取";
            default:
                return action.name();
        }
    }

    private String formatSuccessDetail(Command command, long costMs) {
        return String.format("target=%s:%s %s cost=%dms", command.getOnlyCode(), describeChannel(command.getChannel()),
                safeFuncName(command.getChannel()), costMs);
    }

    private static final class PendingInstruction {
        private final String onlyCode;
        private final int channel;
        private final DeviceControl.Action action;
        private final String controlOnlyCode;
        private final int controlChannel;
        private final String reason;

        private PendingInstruction(String onlyCode, int channel, DeviceControl.Action action,
                                   String controlOnlyCode, int controlChannel, String reason) {
            this.onlyCode = onlyCode;
            this.channel = channel;
            this.action = action;
            this.controlOnlyCode = controlOnlyCode;
            this.controlChannel = controlChannel;
            this.reason = reason;
        }
    }
}
