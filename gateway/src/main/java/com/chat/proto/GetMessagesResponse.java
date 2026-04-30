package com.chat.proto;

import java.util.List;
public final class GetMessagesResponse {
    private final List<ChatMessage> messages;
    public GetMessagesResponse(List<ChatMessage> messages) { this.messages = messages; }
    public List<ChatMessage> getMessages() { return messages; }
}
