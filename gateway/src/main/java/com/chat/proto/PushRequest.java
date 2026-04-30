package com.chat.proto;

public final class PushRequest {
    private final String connectionId;
    private final String gatewayInstanceId;
    private final ChatMessage message;
    public PushRequest(String connectionId, String gatewayInstanceId, ChatMessage message) {
        this.connectionId = connectionId; this.gatewayInstanceId = gatewayInstanceId; this.message = message;
    }
    public String getConnectionId() { return connectionId; }
    public String getGatewayInstanceId() { return gatewayInstanceId; }
    public ChatMessage getMessage() { return message; }
}
