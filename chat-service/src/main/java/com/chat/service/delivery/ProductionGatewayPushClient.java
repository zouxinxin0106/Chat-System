package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.PushResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductionGatewayPushClient implements GatewayPushClient {
    private static final Logger log = LoggerFactory.getLogger(ProductionGatewayPushClient.class);

    public ProductionGatewayPushClient() {
        log.warn("ProductionGatewayPushClient is a stub - not implemented yet");
    }

    @Override
    public CompletableFuture<PushResponse> pushToConnection(String connectionId, String gatewayInstanceId, ChatMessage message) {
        log.warn("ProductionGatewayPushClient: pushToConnection called - not implemented");
        return CompletableFuture.completedFuture(new PushResponse(false, "Not implemented"));
    }

    @Override
    public CompletableFuture<List<PushResponse>> pushToConnections(List<String> connectionIds,
                                                                    String gatewayInstanceId,
                                                                    ChatMessage message) {
        log.warn("ProductionGatewayPushClient: pushToConnections called - not implemented");
        return CompletableFuture.completedFuture(
                connectionIds.stream().map(id -> new PushResponse(false, "Not implemented")).toList()
        );
    }
}