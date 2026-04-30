package com.chat.service.model;

public final class ReadReceiptContent {
    private final String conversationId;
    private final String lastReadMessageId;

    public ReadReceiptContent(String conversationId, String lastReadMessageId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("conversationId cannot be null");
        }
        this.conversationId = conversationId;
        this.lastReadMessageId = lastReadMessageId;
    }

    public String conversationId() { return conversationId; }
    public String lastReadMessageId() { return lastReadMessageId; }
}
