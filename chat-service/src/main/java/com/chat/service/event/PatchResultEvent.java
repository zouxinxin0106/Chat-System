package com.chat.service.event;

import com.chat.service.model.ChatMessage;
import java.util.List;

public final class PatchResultEvent {
    private final String conversationId;
    private final String requesterId;
    private final List<ChatMessage> messages;

    public PatchResultEvent(String conversationId, String requesterId, List<ChatMessage> messages) {
        this.conversationId = conversationId;
        this.requesterId = requesterId;
        this.messages = messages;
    }

    public String conversationId() { return conversationId; }
    public String requesterId() { return requesterId; }
    public List<ChatMessage> messages() { return messages; }
}