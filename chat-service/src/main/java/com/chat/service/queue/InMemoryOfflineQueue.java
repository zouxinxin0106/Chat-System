package com.chat.service.queue;

import org.springframework.stereotype.Repository;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOfflineQueue implements OfflineQueue {

    private final Map<String, ArrayDeque<OfflineMessage>> queues = new ConcurrentHashMap<>();

    @Override
    public void enqueue(OfflineMessage message) {
        queues.computeIfAbsent(message.recipientId(), k -> new ArrayDeque<>())
              .addLast(message);
    }

    @Override
    public OfflineMessage dequeue(String recipientId) {
        var queue = queues.get(recipientId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        return queue.removeFirst();
    }

    @Override
    public boolean isEmpty(String recipientId) {
        var queue = queues.get(recipientId);
        return queue == null || queue.isEmpty();
    }
}
