package com.chat.service.queue;

public interface OfflineQueue {
    void enqueue(OfflineMessage message);
    OfflineMessage dequeue(String recipientId);
    boolean isEmpty(String recipientId);
}
