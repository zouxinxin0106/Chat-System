package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.PushResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface GatewayPushClient {
    CompletableFuture<PushResponse> pushToConnection(String connectionId, String gatewayInstanceId, ChatMessage message);
    CompletableFuture<List<PushResponse>> pushToConnections(List<String> connectionIds, String gatewayInstanceId, ChatMessage message);
    CompletableFuture<List<PushResponse>> pushBatch(Map<String, List<String>> gatewayToConnectionIds, ChatMessage message);
}