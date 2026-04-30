package com.chat.common.logging;

import com.chat.common.tracing.CorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StructuredLogger {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger;
    private final Class<?> clazz;

    public StructuredLogger(Class<?> clazz) {
        this.clazz = clazz;
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void info(String message) {
        info(message, Map.of());
    }

    public void info(String message, Map<String, Object> fields) {
        if (logger.isInfoEnabled()) {
            logger.info(buildLogJson(message, fields, null));
        }
    }

    public void warn(String message) {
        warn(message, Map.of());
    }

    public void warn(String message, Map<String, Object> fields) {
        if (logger.isWarnEnabled()) {
            logger.warn(buildLogJson(message, fields, null));
        }
    }

    public void error(String message, Throwable t) {
        error(message, Map.of(), t);
    }

    public void error(String message, Map<String, Object> fields, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(buildLogJson(message, fields, t));
        }
    }

    public void debug(String message) {
        debug(message, Map.of());
    }

    public void debug(String message, Map<String, Object> fields) {
        if (logger.isDebugEnabled()) {
            logger.debug(buildLogJson(message, fields, null));
        }
    }

    private String buildLogJson(String message, Map<String, Object> fields, Throwable t) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("timestamp", java.time.Instant.now().toString());
            node.put("level", getLevel());
            node.put("message", message);
            node.put("correlation_id", CorrelationId.getOrGenerate());
            if (clazz != null) {
                node.put("logger", clazz.getName());
            }
            if (t != null) {
                node.put("error", t.getMessage());
                node.put("error_type", t.getClass().getName());
            }
            fields.forEach((k, v) -> {
                if (v != null) {
                    node.put(k, v.toString());
                }
            });
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"message\":\"" + message + "\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getLevel() {
        if (logger.isErrorEnabled()) return "ERROR";
        if (logger.isWarnEnabled()) return "WARN";
        if (logger.isInfoEnabled()) return "INFO";
        return "DEBUG";
    }

    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }
}
