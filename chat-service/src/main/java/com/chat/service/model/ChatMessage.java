package com.chat.service.model;

import java.time.Instant;

public final class ChatMessage {
    private String messageId;
    private String conversationId;
    private String senderId;
    private MessageType type;
    private Object content;
    private long sequenceId;
    private long createAt;
    private DeliveryStatus status;
    private String correlationId;

    public ChatMessage() {
    }

    public ChatMessage(String messageId, String conversationId, String senderId, MessageType type,
                       Object content, long sequenceId, long createAt, DeliveryStatus status,
                       String correlationId) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.type = type;
        this.content = content;
        this.sequenceId = sequenceId;
        this.createAt = createAt;
        this.status = status;
        this.correlationId = correlationId;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Object getContent() { return content; }
    public void setContent(Object content) { this.content = content; }

    public long getSequenceId() { return sequenceId; }
    public void setSequenceId(long sequenceId) { this.sequenceId = sequenceId; }

    public long getCreateAt() { return createAt; }
    public void setCreateAt(long createAt) { this.createAt = createAt; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String messageId;
        private String conversationId;
        private String senderId;
        private MessageType type;
        private Object content;
        private long sequenceId;
        private long createAt;
        private DeliveryStatus status;
        private String correlationId;

        public Builder messageId(String messageId) { this.messageId = messageId; return this; }
        public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public Builder senderId(String senderId) { this.senderId = senderId; return this; }
        public Builder type(MessageType type) { this.type = type; return this; }
        public Builder content(Object content) { this.content = content; return this; }
        public Builder sequenceId(long sequenceId) { this.sequenceId = sequenceId; return this; }
        public Builder createAt(long createAt) { this.createAt = createAt; return this; }
        public Builder status(DeliveryStatus status) { this.status = status; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

        public ChatMessage build() {
            return new ChatMessage(messageId, conversationId, senderId, type, content, 
                                   sequenceId, createAt, status, correlationId);
        }
    }
}
