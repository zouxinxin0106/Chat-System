package com.chat.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class LocalEventBus implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(LocalEventBus.class);

    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(Object event) {
        Class<?> eventType = event.getClass();
        List<Consumer<Object>> handlers = subscribers.get(eventType);

        if (handlers == null || handlers.isEmpty()) {
            logger.trace("No subscribers for event type {}", eventType.getSimpleName());
            return;
        }

        List<Consumer<Object>> handlersCopy = new ArrayList<>(handlers);

        for (Consumer<Object> handler : handlersCopy) {
            try {
                handler.accept(event);
            } catch (Exception e) {
                logger.error("Error handling event {} in handler for {}",
                    eventType.getSimpleName(), handler, e);
            }
        }
    }

    @Override
    public void subscribe(Class<?> eventType, Consumer<Object> handler) {
        subscribers
            .computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(handler);

        logger.debug("Subscribed handler for event type {}", eventType.getSimpleName());
    }

    @Override
    public void unsubscribe(Class<?> eventType) {
        List<Consumer<Object>> removed = subscribers.remove(eventType);
        if (removed != null) {
            logger.debug("Unsubscribed {} handlers for event type {}", removed.size(), eventType.getSimpleName());
        }
    }
}
