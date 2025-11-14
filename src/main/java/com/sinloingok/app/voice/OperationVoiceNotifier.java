package com.sinloingok.app.voice;

import com.sinloingok.app.config.SmyooProperties;
import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.models.DeviceControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationVoiceNotifier {

    private static final Map<Integer, String> OPERATION_NAMES = new HashMap<>();

    static {
        OPERATION_NAMES.put(1, "泡沫");
        OPERATION_NAMES.put(2, "洗手");
        OPERATION_NAMES.put(5, "鍍膜");
        OPERATION_NAMES.put(7, "吸塵");
        OPERATION_NAMES.put(8, "清水1");
    }

    private final VoicePromptService voicePromptService;
    private final SmyooProperties smyooProperties;

    public void onChannelEvent(String onlyCode, int channel, DeviceControl.Action action) {
        if (!isVoiceEnabled()) {
            return;
        }
        if (!isTargetDevice(onlyCode)) {
            return;
        }
        if (!OPERATION_NAMES.containsKey(channel)) {
            return;
        }
        if (!(DeviceControl.Action.OPEN.equals(action) || DeviceControl.Action.CLOSE.equals(action))) {
            return;
        }

        String operationName = OPERATION_NAMES.get(channel);
        String message = operationName + (DeviceControl.Action.OPEN.equals(action) ? "操作已啟動" : "操作已結束");
        log.info("voice notify operation: onlyCode={} channel={} action={} message={}", onlyCode, channel, action, message);
        voicePromptService.playText(message);
    }

    public void onSettlement(String onlyCode) {
        if (!isVoiceEnabled() || !isTargetDevice(onlyCode)) {
            return;
        }
        voicePromptService.playText("結算已成功");
    }

    private boolean isVoiceEnabled() {
        return smyooProperties != null && smyooProperties.isEnabled();
    }

    private boolean isTargetDevice(String onlyCode) {
        if (StringUtils.isBlank(onlyCode)) {
            return false;
        }
        int index = SignalTopology.tempWscNetSitePmChNum(onlyCode);
        return index == 1;
    }
}

