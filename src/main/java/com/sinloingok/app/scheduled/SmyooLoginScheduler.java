package com.sinloingok.app.scheduled;

import com.sinloingok.app.config.SmyooProperties;
import com.sinloingok.app.voice.SmyooVoiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmyooLoginScheduler {

    private final SmyooVoiceGateway voiceGateway;
    private final SmyooProperties smyooProperties;

    @Scheduled(cron = "${smyoo.scheduler.cron:0 0 4 * * *}")
    public void refreshLogin() {
        if (!smyooProperties.isEnabled()) {
            return;
        }
        boolean ok = voiceGateway.refreshLogin();
        log.debug("smyoo login scheduler executed, success={}", ok);
    }
}

