package com.sinloingok.app.voice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoicePromptService {

    private final SmyooVoiceGateway voiceGateway;
    private final ResourceLoader resourceLoader;

    private Map<String, String> promptTemplates = new HashMap<>();

    @PostConstruct
    public void loadPrompts() {
        Resource resource = resourceLoader.getResource("classpath:voice-prompts.yml");
        if (!resource.exists()) {
            log.warn("voice prompt resource not found");
            promptTemplates = Collections.emptyMap();
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            if (root == null) {
                promptTemplates = Collections.emptyMap();
                return;
            }
            Map<String, Object> voice = asMap(root.get("voice"));
            Map<String, Object> prompts = asMap(voice.get("prompts"));
            Map<String, String> codes = new HashMap<>();
            for (Map.Entry<String, Object> entry : prompts.entrySet()) {
                if (entry.getValue() != null) {
                    codes.put(entry.getKey(), entry.getValue().toString());
                }
            }
            promptTemplates = Collections.unmodifiableMap(codes);
        } catch (Exception ex) {
            log.error("failed to load voice prompts", ex);
            promptTemplates = Collections.emptyMap();
        }
    }

    public String getPrompt(String code, Map<String, String> data) {
        if (StringUtils.isBlank(code)) {
            return StringUtils.EMPTY;
        }
        String template = promptTemplates.get(code);
        if (StringUtils.isBlank(template)) {
            return StringUtils.EMPTY;
        }
        if (data == null || data.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = StringUtils.replace(result, placeholder, entry.getValue());
        }
        return result;
    }

    public void playText(String text) {
        voiceGateway.playText(text);
    }

    private Map<String, Object> asMap(Object source) {
        if (source instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) source;
        }
        return Collections.emptyMap();
    }
}

