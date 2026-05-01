package com.chat.service.handler;

import com.chat.service.model.MessageType;
import com.chat.service.model.ProcessedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public final class HandlerRegistry {
    private static final Logger log = LoggerFactory.getLogger(HandlerRegistry.class);

    private final Map<MessageType, MessageHandler> handlers;

    @Autowired
    public HandlerRegistry(List<MessageHandler> handlerList) {
        Map<MessageType, MessageHandler> map = new HashMap<>();
        for (MessageHandler handler : handlerList) {
            try {
                MessageType type = (MessageType) handler.getClass().getDeclaredField("MESSAGE_TYPE").get(null);
                map.put(type, handler);
            } catch (Exception e) {
                log.warn("Could not register handler {}: {}", handler.getClass().getName(), e.getMessage());
            }
        }
        this.handlers = map;
        log.info("HandlerRegistry initialized with {} handlers", handlers.size());
    }

    public Optional<MessageHandler> getHandler(MessageType type) {
        return Optional.ofNullable(handlers.get(type));
    }

    public void registerHandler(MessageType type, MessageHandler handler) {
        handlers.put(type, handler);
        log.info("Registered handler for message type: {}", type);
    }

    public List<DeliveryResult> handleBatch(List<ProcessedMessage> messages) {
        Map<MessageType, List<ProcessedMessage>> byType = new HashMap<>();
        for (ProcessedMessage pm : messages) {
            byType.computeIfAbsent(pm.getMessage().getType(), k -> new ArrayList<>()).add(pm);
        }

        List<DeliveryResult> results = new ArrayList<>();
        for (Map.Entry<MessageType, List<ProcessedMessage>> entry : byType.entrySet()) {
            MessageHandler handler = handlers.get(entry.getKey());
            if (handler != null) {
                results.addAll(handler.handleBatch(entry.getValue()));
            }
        }
        return results;
    }
}
