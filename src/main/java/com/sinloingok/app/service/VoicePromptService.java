package com.sinloingok.app.service;

import com.sinloingok.app.constant.VoicePrompts;
import com.sinloingok.app.util.smyoo.VoiceRenderUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VoicePromptService {

    /**
     * 根據場景ID與上下文變量返回最終播報語句
     */
    public String getPrompt(String sceneId, Map<String, String> vars) {
        for (VoicePrompts.Scene s : VoicePrompts.Scene.values()) {
            if (s.getId().equals(sceneId)) {
                return VoiceRenderUtils.render(s, vars);
            }
        }
        return "未知場景：" + sceneId;
    }

}
