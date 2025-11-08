package com.sinloingok.app.util.smyoo;

import com.sinloingok.app.dtos.smyoo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 思麓接口 HTTP 傳輸層的簡化封裝，當前以日誌輸出為主，留待後續接入真實 HTTP 請求。
 */
@Slf4j
@Component
public class HttpTransport {

    public <T> ApiResponse<T> postForObject(String endpoint, Object payload, Class<T> type) {
        log.debug("Simulate POST to {} with payload {}", endpoint, payload);
        return ApiResponse.success(null);
    }

    public ApiResponse<Map<String, Object>> postForMap(String endpoint, Object payload) {
        log.debug("Simulate POST to {} with payload {}", endpoint, payload);
        return ApiResponse.success(new HashMap<>());
    }
}

