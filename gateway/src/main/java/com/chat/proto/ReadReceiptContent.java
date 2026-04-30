package com.chat.proto;

public final class ReadReceiptContent {
    private final String conversationId;
    private final String lastReadMessageId;
    public ReadReceiptContent(String conversationId, String lastReadMessageId) {
        this.conversationId = conversationId; this.lastReadMessageId = lastReadMessageId;
    }
    public String getConversationId() { return conversationId; }
    public String getLastReadMessageId() { return lastReadMessageId; }
}
