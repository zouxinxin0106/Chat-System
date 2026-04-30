package com.chat.proto;

public final class RemoveSessionRequest {
    private final String connectionId;
    public RemoveSessionRequest(String connectionId) { this.connectionId = connectionId; }
    public String getConnectionId() { return connectionId; }
}
