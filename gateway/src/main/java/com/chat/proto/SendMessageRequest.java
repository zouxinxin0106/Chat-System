package com.chat.proto;

public final class SendMessageRequest {
    private final ChatMessage message;
    public SendMessageRequest(ChatMessage message) { this.message = message; }
    public ChatMessage getMessage() { return message; }

    public static Builder newBuilder() { return new Builder(); }
    public static final class Builder {
        private ChatMessage message;
        public Builder setMessage(ChatMessage v) { message = v; return this; }
        public SendMessageRequest build() { return new SendMessageRequest(message); }
    }
}
