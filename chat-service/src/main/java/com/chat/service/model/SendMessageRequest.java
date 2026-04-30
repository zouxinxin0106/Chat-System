package com.chat.service.model;

public final class SendMessageRequest {
    private ChatMessage message;

    public SendMessageRequest() {
    }

    public SendMessageRequest(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }
}
