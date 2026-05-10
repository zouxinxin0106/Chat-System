package com.chat.service.model;

public final class PatchContent {
    private final String conversationId;
    private final long fromSequenceId;
    private final long toSequenceId;

    public PatchContent(String conversationId, long fromSequenceId, long toSequenceId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("conversationId cannot be null");
        }
        if (fromSequenceId > toSequenceId) {
            throw new IllegalArgumentException("fromSequenceId cannot be greater than toSequenceId");
        }
        this.conversationId = conversationId;
        this.fromSequenceId = fromSequenceId;
        this.toSequenceId = toSequenceId;
    }

    public String conversationId() { return conversationId; }
    public long fromSequenceId() { return fromSequenceId; }
    public long toSequenceId() { return toSequenceId; }
}