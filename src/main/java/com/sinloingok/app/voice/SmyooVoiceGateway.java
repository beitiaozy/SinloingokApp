package com.sinloingok.app.voice;

import com.sinloingok.app.config.SmyooProperties;
import com.sinloingok.app.constant.CommonParams;
import com.sinloingok.app.dtos.smyoo.ApiResponse;
import com.sinloingok.app.dtos.smyoo.SmyooClient;
import com.sinloingok.app.dtos.smyoo.req.DatapointRequest;
import com.sinloingok.app.dtos.smyoo.req.LoginRequest;
import com.sinloingok.app.util.smyoo.DatapointBuilder;
import com.sinloingok.app.util.smyoo.ParamsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmyooVoiceGateway {

    private final SmyooClient smyooClient;
    private final SmyooProperties properties;

    private final AtomicReference<SmyooSession> sessionRef = new AtomicReference<>();

    public boolean refreshLogin() {
        if (!properties.isEnabled()) {
            return false;
        }
        SmyooProperties.Auth auth = properties.getAuth();
        SmyooProperties.Common commonProp = properties.getCommon();
        if (auth == null || commonProp == null || StringUtils.isAnyBlank(auth.getPhone(), auth.getPassword(), auth.getClientSecret())
                || commonProp.getUid() == null || StringUtils.isAnyBlank(commonProp.getClientId(), commonProp.getDeviceId())) {
            log.warn("smyoo login skipped: configuration incomplete");
            return false;
        }

        CommonParams common = ParamsFactory.of(commonProp.getUid(), commonProp.getDeviceId(), commonProp.getClientId(),
                Optional.ofNullable(commonProp.getProtocol()).orElse(1));

        LoginRequest request = new LoginRequest(common);
        request.setPhone(auth.getPhone());
        request.setPassword(auth.getPassword());
        request.setClient_secret(auth.getClientSecret());

        ApiResponse<Map<String, Object>> response = smyooClient.login(request);
        if (!response.isOk()) {
            log.warn("smyoo login failed: code={} msg={}", response.getResultCode(), response.getResultMsg());
            return false;
        }

        Map<String, Object> data = Optional.ofNullable(response.getData()).orElseGet(java.util.HashMap::new);
        String ticket = Optional.ofNullable(data.get("ticket")).map(Object::toString).orElse(UUID.randomUUID().toString());
        String sessionId = Optional.ofNullable(data.get("sessionId")).map(Object::toString).orElse(UUID.randomUUID().toString());
        SmyooSession session = new SmyooSession(sessionId, ticket, Instant.now());
        sessionRef.set(session);
        log.info("smyoo login refreshed at {}", session.getLoginTime());
        return true;
    }

    public void playText(String text) {
        if (!properties.isEnabled()) {
            return;
        }
        if (StringUtils.isBlank(text)) {
            return;
        }
        ensureSession();

        SmyooProperties.Speaker speaker = properties.getSpeaker();
        SmyooProperties.Common commonProp = properties.getCommon();
        if (speaker == null || StringUtils.isBlank(speaker.getMcuid()) || commonProp == null || commonProp.getUid() == null
                || StringUtils.isAnyBlank(commonProp.getClientId(), commonProp.getDeviceId())) {
            log.warn("smyoo playback skipped due to incomplete configuration");
            return;
        }

        CommonParams common = ParamsFactory.of(commonProp.getUid(), commonProp.getDeviceId(), commonProp.getClientId(),
                Optional.ofNullable(commonProp.getProtocol()).orElse(1));
        SmyooSession session = sessionRef.get();
        if (session != null) {
            common.setSessionId(session.getSessionId());
            common.setTicket(session.getTicket());
        }

        DatapointRequest request = new DatapointRequest(common);
        request.setMcuid(speaker.getMcuid());
        request.setDatatype(Optional.ofNullable(speaker.getDatatype()).orElse(0));
        request.setDatapoint(DatapointBuilder.create()
                .put("text", text)
                .put("spd", speaker.getSpeed())
                .put("pit", speaker.getPitch())
                .put("vol", speaker.getVolume())
                .put("per", speaker.getPerson())
                .json());

        ApiResponse<Void> response = smyooClient.speakerPlayText(request);
        if (!response.isOk()) {
            log.warn("smyoo playback failed: code={} msg={}", response.getResultCode(), response.getResultMsg());
        }
    }

    private void ensureSession() {
        SmyooSession session = sessionRef.get();
        if (session == null || session.isExpired(properties.getSessionTtl())) {
            refreshLogin();
        }
    }
}

