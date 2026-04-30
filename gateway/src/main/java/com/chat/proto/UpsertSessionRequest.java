package com.chat.proto;

public final class UpsertSessionRequest {
    private final String userId;
    private final String connectionId;
    private final String gatewayInstanceId;
    private final long connectedAt;
    public UpsertSessionRequest(String userId, String connectionId, String gatewayInstanceId, long connectedAt) {
        this.userId = userId; this.connectionId = connectionId;
        this.gatewayInstanceId = gatewayInstanceId; this.connectedAt = connectedAt;
    }
    public String getUserId() { return userId; }
    public String getConnectionId() { return connectionId; }
    public String getGatewayInstanceId() { return gatewayInstanceId; }
    public long getConnectedAt() { return connectedAt; }
}
