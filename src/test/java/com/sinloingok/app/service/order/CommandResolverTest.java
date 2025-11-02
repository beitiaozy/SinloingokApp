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
        for (int ch : channels) {
            CommandResolver.DualCommand first = CommandResolver.resolve(ONLY_CODE, ch, DeviceControl.Action.OPEN);
            assertDualCommand(first, ch, DeviceControl.Action.OPEN);
            applyDual(first);

            CommandResolver.DualCommand second = CommandResolver.resolve(ONLY_CODE, ch, DeviceControl.Action.OPEN);
            assertDualCommand(second, ch, DeviceControl.Action.CLOSE);
            applyDual(second);
        }
    }

    @Test
    public void qs2CommandsRequireQs1OpenState() {
        CommandResolver.DualCommand qs2WhenQs1Closed = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNull(qs2WhenQs1Closed.getControl());
        assertNull(qs2WhenQs1Closed.getSettle());

        CommandResolver.DualCommand qs1OpenControl = CommandResolver.resolve(ONLY_CODE, 8, DeviceControl.Action.OPEN);
        assertDualCommand(qs1OpenControl, 8, DeviceControl.Action.OPEN);
        applyDual(qs1OpenControl);

        CommandResolver.DualCommand qs2Open = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNotNull(qs2Open.getControl());
        assertEquals(8, qs2Open.getControl().getChannel());
        assertEquals(DeviceControl.Action.OPEN, qs2Open.getControl().getCommand());
        applyDual(qs2Open);
        updateLogicalChannelState(3, qs2Open.getControl().getCommand());

        CommandResolver.DualCommand qs2CloseIgnored = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.CLOSE);
        assertNull(qs2CloseIgnored.getControl());
        assertNull(qs2CloseIgnored.getSettle());

        CommandResolver.DualCommand qs2OpenToClose = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNotNull(qs2OpenToClose.getControl());
        assertEquals(8, qs2OpenToClose.getControl().getChannel());
        assertEquals(DeviceControl.Action.CLOSE, qs2OpenToClose.getControl().getCommand());
        applyDual(qs2OpenToClose);
        updateLogicalChannelState(3, qs2OpenToClose.getControl().getCommand());

        CommandResolver.DualCommand qs2OpenWithQs1Closed = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.OPEN);
        assertNull(qs2OpenWithQs1Closed.getControl());
        assertNull(qs2OpenWithQs1Closed.getSettle());

        CommandResolver.DualCommand qs2CloseWithQs1Closed = CommandResolver.resolve(ONLY_CODE, 3, DeviceControl.Action.CLOSE);
        assertNull(qs2CloseWithQs1Closed.getControl());
        assertNull(qs2CloseWithQs1Closed.getSettle());
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
}
