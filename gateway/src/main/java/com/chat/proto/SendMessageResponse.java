package com.chat.proto;

public final class SendMessageResponse {
    private final String messageId;
    private final DeliveryStatus status;
    private final long sequenceId;
    public SendMessageResponse(String messageId, DeliveryStatus status, long sequenceId) {
        this.messageId = messageId; this.status = status; this.sequenceId = sequenceId;
    }
    public String getMessageId() { return messageId; }
    public DeliveryStatus getStatus() { return status; }
    public long getSequenceId() { return sequenceId; }

    public static Builder newBuilder() { return new Builder(); }
    public static final class Builder {
        private String messageId;
        private DeliveryStatus status;
        private long sequenceId;
        public Builder setMessageId(String v) { messageId = v; return this; }
        public Builder setStatus(DeliveryStatus v) { status = v; return this; }
        public Builder setSequenceId(long v) { sequenceId = v; return this; }
        public SendMessageResponse build() { return new SendMessageResponse(messageId, status, sequenceId); }
    }
}
