package com.chat.proto;

public final class GetMessagesRequest {
    private final String conversationId;
    private final String fromMessageId;
    private final int limit;
    public GetMessagesRequest(String conversationId, String fromMessageId, int limit) {
        this.conversationId = conversationId; this.fromMessageId = fromMessageId; this.limit = limit;
    }
    public String getConversationId() { return conversationId; }
    public String getFromMessageId() { return fromMessageId; }
    public int getLimit() { return limit; }
}
