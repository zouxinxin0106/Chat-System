package com.chat.common.session.model;

import java.time.Instant;

public final class ConnectionInfo {
    private final String connectionId;
    private final String gatewayInstanceId;
    private final String userId;
    private final Instant connectedAt;
    private final Instant expiresAt;

    public ConnectionInfo(
            String connectionId,
            String gatewayInstanceId,
            String userId,
            Instant connectedAt,
            Instant expiresAt) {
        this.connectionId = connectionId;
        this.gatewayInstanceId = gatewayInstanceId;
        this.userId = userId;
        this.connectedAt = connectedAt;
        this.expiresAt = expiresAt;
    }

    public String connectionId() { return connectionId; }
    public String gatewayInstanceId() { return gatewayInstanceId; }
    public String userId() { return userId; }
    public Instant connectedAt() { return connectedAt; }
    public Instant expiresAt() { return expiresAt; }
}
