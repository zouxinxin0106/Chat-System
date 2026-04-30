package com.chat.common.tracing;

import java.util.UUID;

public class CorrelationId {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    public static String get() {
        return HOLDER.get();
    }

    public static void set(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static String generate() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String parentId = "0";
        String flags = "01";
        String correlationId = "00-" + traceId + "-" + parentId + "-" + flags;
        HOLDER.set(correlationId);
        return correlationId;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static String getOrGenerate() {
        String existing = HOLDER.get();
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        return generate();
    }
}
