package com.sinloingok.app.service.order;

import com.sinloingok.app.models.Command;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class CommandResolverTest {

    private static final String ONLY_CODE = "TEST-ONLY-CODE";

    private DeviceControl deviceControl;

    @Before
    public void setUp() {
        NetSite site = new NetSite(ONLY_CODE);
        site.setPurpose(NetSiteCache.PURPOSE_WSC);
        NetSiteCache.refresh(Collections.singleton(site));
        deviceControl = new DeviceControl(site);
        NetSiteCache.refreshDeviceControl(ONLY_CODE, deviceControl);
        resetChannelStates();
    }

    @Test
    public void autoToggleChannelsRespondToSequentialOpenCommands() {
        int[] channels = {2, 4, 5, 7, 8};
        for (int channel : channels) {
            DeviceControl.Action expectedFirst = predictedToggleAction(channel);
            DeviceControl.Action firstResult = toggleChannelAndReturnAction(channel);
            assertEquals(expectedFirst, firstResult);

            DeviceControl.Action expectedSecond = predictedToggleAction(channel);
            DeviceControl.Action secondResult = toggleChannelAndReturnAction(channel);
            assertEquals(expectedSecond, secondResult);
        }
    }

    @Test
    public void forwardUsageFlowWithRandomQs2ThenPostServicesAndSettlement() {
        // 正向流程：先開 QS1 保障供水，隨後模擬 QS2 在供水過程中隨機多次開關。
        DeviceControl.Action qs1Start = toggleChannelAndReturnAction(8);
        assertEquals(DeviceControl.Action.OPEN, qs1Start);

        DeviceControl.Action firstQs2 = toggleChannelAndReturnAction(3);
        assertEquals(DeviceControl.Action.OPEN, firstQs2);
        DeviceControl.Action secondQs2 = toggleChannelAndReturnAction(3);
        assertEquals(DeviceControl.Action.CLOSE, secondQs2);
        DeviceControl.Action thirdQs2 = toggleChannelAndReturnAction(3);
        assertEquals(DeviceControl.Action.OPEN, thirdQs2);

        // 正向流程：關閉 QS1 後，嘗試再次開 QS2 應被忽略（無供水不可用）。
        DeviceControl.Action qs1Stop = toggleChannelAndReturnAction(8);
        assertEquals(DeviceControl.Action.CLOSE, qs1Stop);
        CommandResolver.DualCommand qs2Ignored = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNull(qs2Ignored.getControl());
        assertNull(qs2Ignored.getSettle());

        // 正向流程：依次模擬吸塵(XC, ch7)、洗手(XS, ch2)、鍍膜(DM, ch5) 多次開關。
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 7, 3);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 2, 2);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 5, 4);

        // 正向流程：最後下發結算（關機）指令，確認控制與結算信號。
        CommandResolver.DualCommand settlement = CommandResolver.resolve(ONLY_CODE, 6, DeviceControl.Action.CLOSE);
        assertShutdownSettlement(settlement);
    }

    @Test
    public void reverseUsageFlowWithLateQs1ActivationAndMultipleServices() {
        // 逆向流程：尚未開 QS1 前，嘗試 QS2 應被拒絕。
        CommandResolver.DualCommand qs2BeforeQs1 = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNull(qs2BeforeQs1.getControl());
        assertNull(qs2BeforeQs1.getSettle());

        // 逆向流程：先操作其它服務（吸塵、洗手、鍍膜），確保自動交替仍然生效。
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 7, 1);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 2, 1);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 5, 1);

        // 逆向流程：此時補開 QS1，允許後續 QS2 指令，並覆蓋再次隨機開關。
        DeviceControl.Action qs1Enabled = toggleChannelAndReturnAction(8);
        assertEquals(DeviceControl.Action.OPEN, qs1Enabled);
        DeviceControl.Action qs2NowWorks = toggleChannelAndReturnAction(3);
        assertEquals(DeviceControl.Action.OPEN, qs2NowWorks);
        DeviceControl.Action qs2ReToggle = toggleChannelAndReturnAction(3);
        assertEquals(DeviceControl.Action.CLOSE, qs2ReToggle);

        // 逆向流程：關閉 QS1，並確認 QS2 再次失效，同時回收各服務。
        DeviceControl.Action qs1Disabled = toggleChannelAndReturnAction(8);
        assertEquals(DeviceControl.Action.CLOSE, qs1Disabled);
        CommandResolver.DualCommand qs2AfterShutdown = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNull(qs2AfterShutdown.getControl());
        assertNull(qs2AfterShutdown.getSettle());

        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 7, 2);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 2, 2);
        assertServiceCycle(DeviceControl.Action.OPEN, DeviceControl.Action.CLOSE, 5, 2);

        // 逆向流程：最後仍需下發結算確保整個回合正常結束。
        CommandResolver.DualCommand settlement = CommandResolver.resolve(ONLY_CODE, 6, DeviceControl.Action.CLOSE);
        assertShutdownSettlement(settlement);
    }

    private void applyDual(CommandResolver.DualCommand dual) {
        if (dual == null) {
            return;
        }
        Command control = dual.getControl();
        if (control != null) {
            deviceControl.updateState(control.getChannel(), control.getCommand());
        }
        Command settle = dual.getSettle();
        if (settle != null) {
            if (control == null || !settle.equals(control)) {
                deviceControl.updateState(settle.getChannel(), settle.getCommand());
            }
        }
    }

    private void assertDualCommand(CommandResolver.DualCommand dual, int expectedChannel, DeviceControl.Action expectedAction) {
        assertNotNull(dual);
        Command control = dual.getControl();
        Command settle = dual.getSettle();
        assertNotNull(control);
        assertNotNull(settle);
        assertEquals(expectedChannel, control.getChannel());
        assertEquals(expectedAction, control.getCommand());
        assertEquals(control, settle);
    }

    private void updateLogicalChannelState(int channel, DeviceControl.Action action) {
        Map<Integer, DeviceControl.DeviceToggleState> states = deviceControl.getChannelStates();
        DeviceControl.DeviceToggleState toggleState = states.get(channel);
        if (toggleState != null) {
            toggleState.updateAfterExecute(action, System.currentTimeMillis());
        }
    }

    private void resetChannelStates() {
        for (DeviceControl.DeviceToggleState state : deviceControl.getChannelStates().values()) {
            state.updateAfterExecute(DeviceControl.Action.CLOSE, System.currentTimeMillis());
        }
    }

    private DeviceControl.Action toggleChannelAndReturnAction(int requestChannel) {
        DeviceControl.Action predicted = predictedToggleAction(requestChannel);
        CommandResolver.DualCommand dual = CommandResolver.resolve(ONLY_CODE, requestChannel, DeviceControl.Action.OPEN);
        int physicalChannel = requestChannel == 3 ? 8 : requestChannel;
        assertDualCommand(dual, physicalChannel, predicted);
        DeviceControl.Action executedAction = dual.getControl().getCommand();
        applyDual(dual);
        if (requestChannel == 3) {
            updateLogicalChannelState(3, executedAction);
        }
        return executedAction;
    }

    private DeviceControl.Action predictedToggleAction(int requestChannel) {
        int logicalChannel = requestChannel == 3 ? 3 : requestChannel;
        return deviceControl.getChannelStates().get(logicalChannel).toggledCommand();
    }

    private void assertServiceCycle(DeviceControl.Action expectedFirst, DeviceControl.Action expectedSecond, int channel, int cycles) {
        for (int i = 0; i < cycles; i++) {
            DeviceControl.Action first = toggleChannelAndReturnAction(channel);
            assertEquals(expectedFirst, first);
            DeviceControl.Action second = toggleChannelAndReturnAction(channel);
            assertEquals(expectedSecond, second);
        }
    }

    private void assertShutdownSettlement(CommandResolver.DualCommand dual) {
        assertNotNull(dual);
        Command control = dual.getControl();
        Command settle = dual.getSettle();
        assertNotNull(control);
        assertNotNull(settle);
        assertEquals(0, control.getChannel());
        assertEquals(DeviceControl.Action.CLOSE, control.getCommand());
        assertEquals(-1, settle.getChannel());
        assertEquals(DeviceControl.Action.OVER, settle.getCommand());
    }
}
