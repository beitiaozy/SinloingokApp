package com.sinloingok.app.service.order;

import com.sinloingok.app.models.Command;
import com.sinloingok.app.models.DeviceControl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages channel-level locks that require relay feedback before accepting
 * subsequent commands. Currently used to guard QS1 OPEN commands for device
 * {@code 0090D500245B} which shares the physical channel with QS2.
 */
@Slf4j
@Component
public class ChannelLockManager {

    private static final String TARGET_ONLY_CODE = "0090D500245B";
    private static final int TARGET_CHANNEL = 8;
    private static final Duration AUTO_RELEASE = Duration.ofSeconds(5);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "channel-lock-manager");
        t.setDaemon(true);
        return t;
    });

    private final ConcurrentMap<LockKey, LockTicket> locks = new ConcurrentHashMap<>();

    public enum AcquireResult {
        NOT_REQUIRED,
        ACQUIRED,
        BUSY
    }

    public AcquireResult acquire(Command command) {
        if (!shouldLock(command)) {
            return AcquireResult.NOT_REQUIRED;
        }

        LockKey key = new LockKey(command.getOnlyCode(), command.getChannel());
        LockTicket ticket = new LockTicket();
        ticket.setTraceId(MDC.get("trace"));
        LockTicket existing = locks.putIfAbsent(key, ticket);
        if (existing != null) {
            log.info("trace={} phase=lock step=busy onlyCode={} channel={}",
                    MDC.get("trace"), command.getOnlyCode(), command.getChannel());
            return AcquireResult.BUSY;
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> release(key, ticket.getToken(), "auto"),
                AUTO_RELEASE.toMillis(), TimeUnit.MILLISECONDS);
        ticket.setFuture(future);
        log.info("trace={} phase=lock step=acquired onlyCode={} channel={} timeoutMs={}",
                ticket.getTraceId(), command.getOnlyCode(), command.getChannel(), AUTO_RELEASE.toMillis());
        return AcquireResult.ACQUIRED;
    }

    public void release(Command command) {
        release(command.getOnlyCode(), command.getChannel(), "manual");
    }

    public void release(String onlyCode, int channel) {
        release(onlyCode, channel, "manual");
    }

    public void onRelayFeedback(String onlyCode) {
        if (!TARGET_ONLY_CODE.equalsIgnoreCase(onlyCode)) {
            return;
        }
        release(onlyCode, TARGET_CHANNEL, "feedback");
    }

    private boolean shouldLock(Command command) {
        if (command == null) {
            return false;
        }
        if (!TARGET_ONLY_CODE.equalsIgnoreCase(command.getOnlyCode())) {
            return false;
        }
        if (command.getChannel() != TARGET_CHANNEL) {
            return false;
        }
        return DeviceControl.Action.OPEN.equals(command.getCommand());
    }

    private void release(LockKey key, String expectedToken, String reason) {
        LockTicket ticket = locks.get(key);
        if (ticket == null) {
            return;
        }
        if (expectedToken != null && !Objects.equals(expectedToken, ticket.getToken())) {
            return;
        }
        if (locks.remove(key, ticket)) {
            ticket.cancel();
            log.info("trace={} phase=lock step=released onlyCode={} channel={} reason={}",
                    ticket.getTraceId(), key.onlyCode, key.channel, reason);
        }
    }

    private void release(String onlyCode, int channel, String reason) {
        LockKey key = new LockKey(onlyCode, channel);
        LockTicket ticket = locks.get(key);
        if (ticket != null) {
            release(key, ticket.getToken(), reason);
        }
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
    }

    private static final class LockKey {
        private final String onlyCode;
        private final int channel;

        private LockKey(String onlyCode, int channel) {
            this.onlyCode = onlyCode;
            this.channel = channel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LockKey)) return false;
            LockKey lockKey = (LockKey) o;
            return channel == lockKey.channel && Objects.equals(onlyCode, lockKey.onlyCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(onlyCode, channel);
        }
    }

    private static final class LockTicket {
        private final String token = UUID.randomUUID().toString();
        private volatile ScheduledFuture<?> future;
        private volatile String traceId;

        public String getToken() {
            return token;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        public void cancel() {
            if (future != null) {
                future.cancel(false);
            }
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
    }
}
