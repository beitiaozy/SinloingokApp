package com.sinloingok.app.dtos.smyoo;

import com.sinloingok.app.dtos.smyoo.req.DatapointRequest;
import com.sinloingok.app.dtos.smyoo.req.LoginRequest;
import com.sinloingok.app.dtos.smyoo.req.RequestBase;
import com.sinloingok.app.dtos.smyoo.req.TicketVerifyRequest;
import com.sinloingok.app.util.smyoo.HttpTransport;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 思麓雲喇叭 API 客戶端，當前以模擬請求為主，方便在無網絡的情況下調試流程。
 */
@Component
@RequiredArgsConstructor
public class SmyooClient {

    private final HttpTransport httpTransport;

    public ApiResponse<Map<String, Object>> login(LoginRequest request) {
        return httpTransport.postForMap("/open/login", request);
    }

    public ApiResponse<Map<String, Object>> verifyTicket(TicketVerifyRequest request) {
        return httpTransport.postForMap("/open/ticket-verify", request);
    }

    public ApiResponse<Map<String, Object>> statusChanged(RequestBase request) {
        return httpTransport.postForMap("/open/status-changed", request);
    }

    public ApiResponse<Map<String, Object>> queryDevices(RequestBase request) {
        return httpTransport.postForMap("/open/devices", request);
    }

    public ApiResponse<Void> setChannelData(DatapointRequest request) {
        return httpTransport.postForObject("/open/channel-data", request, Void.class);
    }

    public ApiResponse<Void> speakerPlayText(DatapointRequest request) {
        if (request != null && StringUtils.isBlank(request.getDatapoint())) {
            return ApiResponse.failure(-1, "datapoint missing");
        }
        return httpTransport.postForObject("/open/speaker-play-text", request, Void.class);
    }

    public ApiResponse<Void> setCountdownAlarm(DatapointRequest request) {
        return httpTransport.postForObject("/open/countdown-alarm", request, Void.class);
    }
}

