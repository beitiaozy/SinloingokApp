package com.sinloingok.app.util.ns;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接跟踪：记录离线原因 & 离线时间，供日志和排查使用。
 */
@UtilityClass
public class DeviceConnectionTracker {

    private static final Map<String, DisconnectRecord> OFFLINE = new ConcurrentHashMap<>();

    public void markOffline(String onlyCode, String reason) {
        if (onlyCode == null) {
            return;
        }
        OFFLINE.put(onlyCode, new DisconnectRecord(reason == null ? "unknown" : reason));
    }

    public void markOnline(String onlyCode) {
        if (onlyCode == null) {
            return;
        }
        OFFLINE.remove(onlyCode);
    }

    public DisconnectRecord get(String onlyCode) {
        if (onlyCode == null) {
            return null;
        }
        return OFFLINE.get(onlyCode);
    }

    @Getter
    @ToString
    public static class DisconnectRecord {
        private final String reason;
        private final Instant at;

        private DisconnectRecord(String reason) {
            this.reason = reason;
            this.at = Instant.now();
        }
    }
}
