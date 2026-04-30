package com.chat.service.event;

public final class MessageDeliveredEvent {
    private final String messageId;
    private final String recipientId;
    private final long deliveredAt;

    public MessageDeliveredEvent(String messageId, String recipientId, long deliveredAt) {
        this.messageId = messageId;
        this.recipientId = recipientId;
        this.deliveredAt = deliveredAt;
    }

    public String messageId() { return messageId; }
    public String recipientId() { return recipientId; }
    public long deliveredAt() { return deliveredAt; }
}
