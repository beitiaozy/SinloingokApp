package com.sinloingok.app.voice;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
public class SmyooSession {
    String sessionId;
    String ticket;
    Instant loginTime;

    public boolean isExpired(Duration ttl) {
        if (ttl == null) {
            return false;
        }
        return loginTime.plus(ttl).isBefore(Instant.now());
    }
}

