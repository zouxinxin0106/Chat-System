package com.chat.service.logging;

import com.chat.common.logging.StructuredLogger;

public class ServiceLogger {
    private final StructuredLogger logger;

    public ServiceLogger(Class<?> clazz) {
        this.logger = StructuredLogger.getLogger(clazz);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Object... args) {
        logger.info(String.format(message, args));
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public void debug(String message) {
        logger.debug(message);
    }
}
