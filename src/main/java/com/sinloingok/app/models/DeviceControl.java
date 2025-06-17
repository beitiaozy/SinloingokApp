package com.sinloingok.app.models;

import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 每台 NetSite 的状态与通道开关节流（2s）+ 交替（OPEN<->CLOSE）
 */
@Data
public class DeviceControl {

    public enum Action {
        OPEN, CLOSE, READ, OVER;

        public boolean isOption(){
            return this.equals(OPEN) || this.equals(CLOSE);
        }
    }

    public static final String STATUS_USING  = "USING";
    public static final String STATUS_NO_USE = "NO_USE";

    private String onlyCode;

    /** PM 物理执行所在设备编码与通道（用于 ch8 控 PMKZSB 时） */
    private String pmOnlyCode; // 一般是 PMKZSB 的 onlyCode
    private int pmChannel;     // 一般为“当前设备序号”，即 1..8

    private String status;     // using / no_use
    private String payCode;    // 设备订单号
    private NetSite netSite;
    private NetSiteOrder order;

    /** 每通道状态（1..8） */
    private Map<Integer, DeviceToggleState> channelStates = new HashMap<>();

    public DeviceControl(NetSite netSite){
        this.netSite = netSite;
        setStatus(STATUS_NO_USE);
        setOnlyCode(netSite.getOnlyCode());
        for (int ch=1; ch<=8; ch++) channelStates.put(ch, new DeviceToggleState());
    }

    public void setOrder(NetSiteOrder order){
        setPayCode(order.getPayCode());
        setStatus(STATUS_USING);
        this.order = order;
    }

    /** 仅状态更新，供外层在真正下发成功后调用 */
    public void updateState(int channel, Action command) {
        DeviceToggleState s = channelStates.get(channel);
        if (s != null){
            s.updateAfterExecute(command, System.currentTimeMillis());
            channelStates.put(channel, s);
        };
    }

    /** 通道状态 + 限流（2s）+ 交替规则 */
    public static class DeviceToggleState {
        private boolean isOpen = false; // 初始为 CLOSE

        public boolean isOpen() { return isOpen; }

        public void updateAfterExecute(Action command, long now) {
            this.isOpen = Action.OPEN.equals(command);
        }

        /** 自动切换（用于“只有 OPEN 事件”的通道） */
        public Action toggledCommand() {
            return isOpen ? Action.CLOSE : Action.OPEN;
        }
    }
}
