package com.chat.common.event;

import java.util.function.Consumer;

public interface EventBus {

    void publish(Object event);

    void subscribe(Class<?> eventType, Consumer<Object> handler);

    void unsubscribe(Class<?> eventType);
}
