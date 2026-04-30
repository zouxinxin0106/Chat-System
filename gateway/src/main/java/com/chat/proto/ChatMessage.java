package com.chat.proto;

public final class ChatMessage {
    private final String messageId;
    private final String conversationId;
    private final String senderId;
    private final MessageType type;
    private final MessageContent content;
    private final long sequenceId;
    private final long createAt;
    private final DeliveryStatus status;
    private final String correlationId;

    public ChatMessage(String messageId, String conversationId, String senderId, MessageType type,
            MessageContent content, long sequenceId, long createAt, DeliveryStatus status, String correlationId) {
        this.messageId = messageId; this.conversationId = conversationId; this.senderId = senderId;
        this.type = type; this.content = content; this.sequenceId = sequenceId;
        this.createAt = createAt; this.status = status; this.correlationId = correlationId;
    }

    public String getMessageId() { return messageId; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public MessageType getType() { return type; }
    public MessageContent getContent() { return content; }
    public long getSequenceId() { return sequenceId; }
    public long getCreateAt() { return createAt; }
    public DeliveryStatus getStatus() { return status; }
    public String getCorrelationId() { return correlationId; }

    @SuppressWarnings("unchecked")
    public static com.google.protobuf.Parser<ChatMessage> parser() {
        return (com.google.protobuf.Parser<ChatMessage>) new Object();
    }

    public static Builder newBuilder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public static final class Builder {
        private String messageId = "";
        private String conversationId = "";
        private String senderId = "";
        private MessageType type = MessageType.MESSAGE_TYPE_UNSPECIFIED;
        private MessageContent content;
        private long sequenceId;
        private long createAt;
        private DeliveryStatus status = DeliveryStatus.DELIVERY_STATUS_UNSPECIFIED;
        private String correlationId = "";
        public Builder() {}
        private Builder(ChatMessage m) { this.messageId = m.messageId; this.conversationId = m.conversationId; this.senderId = m.senderId; this.type = m.type; this.content = m.content; this.sequenceId = m.sequenceId; this.createAt = m.createAt; this.status = m.status; this.correlationId = m.correlationId; }
        public Builder setMessageId(String v) { messageId = v; return this; }
        public Builder setConversationId(String v) { conversationId = v; return this; }
        public Builder setSenderId(String v) { senderId = v; return this; }
        public Builder setType(MessageType v) { type = v; return this; }
        public Builder setContent(MessageContent v) { content = v; return this; }
        public Builder setSequenceId(long v) { sequenceId = v; return this; }
        public Builder setCreateAt(long v) { createAt = v; return this; }
        public Builder setStatus(DeliveryStatus v) { status = v; return this; }
        public Builder setCorrelationId(String v) { correlationId = v; return this; }
        public ChatMessage build() { return new ChatMessage(messageId, conversationId, senderId, type, content, sequenceId, createAt, status, correlationId); }
    }
}
