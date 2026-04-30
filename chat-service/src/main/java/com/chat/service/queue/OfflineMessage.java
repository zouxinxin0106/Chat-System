package com.chat.service.queue;

import java.time.Instant;

public final class OfflineMessage {
    private final String messageId;
    private final String recipientId;
    private final String conversationId;
    private final long sequenceId;
    private final Instant expiresAt;

    public OfflineMessage(String messageId, String recipientId, String conversationId,
                         long sequenceId, Instant expiresAt) {
        this.messageId = messageId;
        this.recipientId = recipientId;
        this.conversationId = conversationId;
        this.sequenceId = sequenceId;
        this.expiresAt = expiresAt;
    }

    public String messageId() { return messageId; }
    public String recipientId() { return recipientId; }
    public String conversationId() { return conversationId; }
    public long sequenceId() { return sequenceId; }
    public Instant expiresAt() { return expiresAt; }
}
