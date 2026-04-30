package com.chat.service.model;

public final class PushRequest {
    private String connectionId;
    private String gatewayInstanceId;
    private ChatMessage message;

    public PushRequest() {
    }

    public PushRequest(String connectionId, String gatewayInstanceId, ChatMessage message) {
        this.connectionId = connectionId;
        this.gatewayInstanceId = gatewayInstanceId;
        this.message = message;
    }

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

    public String getGatewayInstanceId() { return gatewayInstanceId; }
    public void setGatewayInstanceId(String gatewayInstanceId) { this.gatewayInstanceId = gatewayInstanceId; }

    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }
}
