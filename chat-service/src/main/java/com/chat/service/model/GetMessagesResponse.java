package com.chat.service.model;

import java.util.ArrayList;
import java.util.List;

public final class GetMessagesResponse {
    private List<ChatMessage> messages;

    public GetMessagesResponse() {
        this.messages = new ArrayList<>();
    }

    public GetMessagesResponse(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
