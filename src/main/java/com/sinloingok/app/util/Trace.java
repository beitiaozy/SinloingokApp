package com.sinloingok.app.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class Trace {
    private Trace() {}

    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void bind(String traceId, String onlyCode, int channel, String action, String funcCode, String funcName) {
        // 统一放入MDC，logback 也能在pattern里拿到
        MDC.put("trace", traceId);
        MDC.put("oc", onlyCode);
        MDC.put("chn", String.valueOf(channel));
        MDC.put("act", action);
        MDC.put("fcode", funcCode);
        MDC.put("fname", funcName);
    }

    public static void clear() {
        MDC.clear();
    }
}
