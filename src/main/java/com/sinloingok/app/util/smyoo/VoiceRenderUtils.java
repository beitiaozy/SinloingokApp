package com.sinloingok.app.util.smyoo;

import com.sinloingok.app.constant.VoicePrompts;

import java.util.Map;

public class VoiceRenderUtils {
    public static String render(VoicePrompts.Scene scene, Map<String, String> data) {
        String text = scene.getTemplate();
        for (String var : scene.getVariables()) {
            String placeholder = "{" + var + "}";
            String value = data.getOrDefault(var, "");
            text = text.replace(placeholder, value);
        }
        return text;
    }
}
