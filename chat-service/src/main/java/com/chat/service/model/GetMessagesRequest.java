package com.chat.service.model;

public final class GetMessagesRequest {
    private String conversationId;
    private String fromMessageId;
    private int limit;

    public GetMessagesRequest() {
    }

    public GetMessagesRequest(String conversationId, String fromMessageId, int limit) {
        this.conversationId = conversationId;
        this.fromMessageId = fromMessageId;
        this.limit = limit;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getFromMessageId() { return fromMessageId; }
    public void setFromMessageId(String fromMessageId) { this.fromMessageId = fromMessageId; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
