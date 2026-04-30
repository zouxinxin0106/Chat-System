package com.chat.service.model;

public final class SendMessageResponse {
    private String messageId;
    private DeliveryStatus status;
    private long sequenceId;

    public SendMessageResponse() {
    }

    public SendMessageResponse(String messageId, DeliveryStatus status, long sequenceId) {
        this.messageId = messageId;
        this.status = status;
        this.sequenceId = sequenceId;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public long getSequenceId() { return sequenceId; }
    public void setSequenceId(long sequenceId) { this.sequenceId = sequenceId; }
}
