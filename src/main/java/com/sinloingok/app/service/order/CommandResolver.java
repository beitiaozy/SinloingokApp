package com.sinloingok.app.service.order;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.models.Command;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public final class CommandResolver {

    private CommandResolver() {}

    @Data
    @AllArgsConstructor
    public static class DualCommand {
        /** 实际控制物理设备的指令（可能为 null） */
        private Command control;
        /** 结算用的指令（可能为 null） */
        private Command settle;
    }

    /**
     * 解析 (onlyCode, chNum, commandStr) → control / settle
     *
     * 通道（反转后 1..8）：
     * 1=PM(泡沫) 2=XS(洗手) 3=QS2(清水2) 4=CQ(吹气)
     * 5=DM(镀膜) 6=GJ(关机) 7=XC(吸尘) 8=QS1(清水1)
     *
     * - PMKZSB：落对应站点 PM(ch1) 结算
     * - {2,4,5,7,8}：仅 OPEN → 自动切换（以该通道状态）
     * - ch6(GJ)：仅 CLOSE → 总控0 + 结算全部
     * - ch3(QS2)：仅 OPEN → 以 ch3 状态决策，实际控制/结算都落 ch8(QS1)
     * - ch1(PM)：仅 OPEN，控制 PMKZSB、结算本机 ch1
     */
    public static DualCommand resolve(String onlyCode, int chNum, DeviceControl.Action commandStr) {
        NetSite netSite = NetSiteCache.get(onlyCode);
        if (netSite == null) throw new IllegalArgumentException("unknown_onlyCode=" + onlyCode);
        if (!commandStr.isOption()) throw new IllegalArgumentException("command_must_be_OPEN_CLOSE");

        DeviceControl dc = NetSiteCache.wscDeviceControl(onlyCode);

        // PMKZSB 控制器入口
        if (netSite.getPurpose().startsWith("PM")) {
            String siteCode = SignalTopology.tempWscNetSiteMapping(chNum);
            Command settle  = Command.of(siteCode, 1, commandStr);
            log.debug("trace={} phase=map step=pm ingressCh={} funcName={} funcCode={} settleTarget={}:{} cmd={}",
                    MDC.get("trace"), chNum, safeName(chNum), safeCode(chNum), siteCode, 1, commandStr);
            return new DualCommand(null, settle);
        }

        if (dc == null) {
            log.debug("trace={} phase=map step=guard msg=dc_not_found onlyCode={}", MDC.get("trace"), onlyCode);
            return new DualCommand(null, null);
        }

        switch (chNum) {
            case 2: case 4: case 5: case 7: case 8: {
                if (!DeviceControl.Action.OPEN.equals(commandStr)) return new DualCommand(null, null);
                DeviceControl.Action real = dc.getChannelStates().get(chNum).toggledCommand();
                log.debug("trace={} phase=map step=auto ingressCh={} funcName={} funcCode={} ctrlTarget={}:{} settleTarget={}:{} realCmd={}",
                        MDC.get("trace"), chNum, safeName(chNum), safeCode(chNum), onlyCode, chNum, onlyCode, chNum, real);
                return new DualCommand(Command.of(onlyCode, chNum, real),
                        Command.of(onlyCode, chNum, real));
            }
            case 6: { // 关机
                if (!DeviceControl.Action.CLOSE.equals(commandStr)) return new DualCommand(null, null);
                log.debug("trace={} phase=map step=gj ingressCh=6 funcName={} funcCode={} ctrlTarget={}:{} settle=ALL cmd=CLOSE",
                        MDC.get("trace"), safeName(6), safeCode(6), onlyCode, 0);
                return new DualCommand(Command.of(onlyCode, 0, DeviceControl.Action.CLOSE),
                        Command.of(onlyCode, -1, DeviceControl.Action.OVER));
            }
            case 3: { // QS2 → 决策以ch3，控制/结算落 ch8
//                if (!DeviceControl.Action.OPEN.equals(commandStr)) return new DualCommand(null, null);

//                return new DualCommand(null, null);
                if(!dc.getChannelStates().get(8).isOpen()) return new DualCommand(null, null);
                //  3號  status open
                // 清水1 為開始執行
                log.error("清水2真實狀態{}", commandStr);
                log.debug("trace={} phase=map step=qs2 ingressCh=3 funcName={} funcCode={} decisionBy=3 ctrlTarget={}:{} settleTarget={}:{} realCmd={}",
                        MDC.get("trace"), safeName(3), safeCode(3), onlyCode, 8, onlyCode, 8, commandStr);
                return new DualCommand(Command.of(onlyCode, 8, commandStr),
                        Command.of(onlyCode, 8, commandStr));
            }
            case 1: { // PM
                if (!DeviceControl.Action.OPEN.equals(commandStr)) return new DualCommand(null, null);
                String pmOnlyCode = SignalTopology.tempPmkzsbOnlyCode();
                int pmChNum = SignalTopology.tempWscNetSitePmChNum(onlyCode);
                DeviceControl.Action real = dc.getChannelStates().get(1).toggledCommand();
                log.debug("trace={} phase=map step=pm_local ingressCh=1 funcName={} funcCode={} ctrlTarget={}:{} settleTarget={}:{} ctrlCmd={} settleCmd={}",
                        MDC.get("trace"), safeName(1), safeCode(1), pmOnlyCode, pmChNum, onlyCode, 1, commandStr, real);
                return new DualCommand(Command.of(pmOnlyCode, pmChNum, commandStr),
                        Command.of(onlyCode, 1, real));
            }
            default:
                log.debug("trace={} phase=map step=pass msg=unsupported_channel ch={}", MDC.get("trace"), chNum);
                return new DualCommand(null, null);
        }
    }

    private static String safeName(int ch) {
        try { return SignalTopology.getFunctionName(ch); } catch (Exception e) { return "NA"; }
    }
    private static String safeCode(int ch) {
        try { return SignalTopology.getFunctionCode(ch); } catch (Exception e) { return "NA"; }
    }

    public static Command pmOpenCommand(String onlyCode){
        int siteNo = SignalTopology.tempWscNetSitePmChNum(onlyCode);
        String pmk = SignalTopology.tempPmkzsbOnlyCode();
        log.info("trace={} phase=pm step=open target={}:{}", MDC.get("trace"), pmk, siteNo);
        return Command.of(pmk, siteNo, DeviceControl.Action.OPEN);
    }

    public static Command pmCloseCommand(String onlyCode){
        int siteNo = SignalTopology.tempWscNetSitePmChNum(onlyCode);
        String pmk = SignalTopology.tempPmkzsbOnlyCode();
        log.info("trace={} phase=pm step=close target={}:{}", MDC.get("trace"), pmk, siteNo);
        return Command.of(pmk, siteNo, DeviceControl.Action.CLOSE);
    }
}
