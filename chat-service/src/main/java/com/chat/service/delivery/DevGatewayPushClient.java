package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.PushResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Primary
public class DevGatewayPushClient implements GatewayPushClient {
    private static final Logger log = LoggerFactory.getLogger(DevGatewayPushClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String gatewayHost;
    private final int gatewayPort;
    private final HttpClient httpClient;

    public DevGatewayPushClient(
            @Value("${gateway.host:localhost}") String gatewayHost,
            @Value("${gateway.port:8080}") int gatewayPort) {
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.httpClient = HttpClient.newHttpClient();
        log.info("DevGatewayPushClient initialized with gateway at {}:{}", gatewayHost, gatewayPort);
    }

    @Override
    public CompletableFuture<PushResponse> pushToConnection(String connectionId, String gatewayInstanceId, ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> pushPayload = Map.of(
                        "connection_id", connectionId,
                        "gateway_instance_id", gatewayInstanceId,
                        "message", Map.of(
                                "message_id", message.getMessageId(),
                                "conversation_id", message.getConversationId(),
                                "sender_id", message.getSenderId(),
                                "type", message.getType().name(),
                                "sequence_id", message.getSequenceId(),
                                "correlation_id", message.getCorrelationId() != null ? message.getCorrelationId() : ""
                        )
                );

                String json = objectMapper.writeValueAsString(pushPayload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + gatewayHost + ":" + gatewayPort + "/push"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                log.info("GatewayPushClient: Pushed message {} to connection: {}, response: {}",
                        message.getMessageId(), connectionId, response.statusCode());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return new PushResponse(true, null);
                } else {
                    return new PushResponse(false, "HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                log.error("Failed to push to gateway: {}", e.getMessage(), e);
                return new PushResponse(false, e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<List<PushResponse>> pushToConnections(List<String> connectionIds,
                                                                    String gatewayInstanceId,
                                                                    ChatMessage message) {
        List<CompletableFuture<PushResponse>> futures = connectionIds.stream()
                .map(connId -> pushToConnection(connId, gatewayInstanceId, message))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    @Override
    public CompletableFuture<List<PushResponse>> pushBatch(Map<String, List<String>> gatewayToConnectionIds, ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Map<String, Object>> entries = gatewayToConnectionIds.entrySet().stream()
                        .map(e -> Map.of(
                                "connection_ids", e.getValue(),
                                "gateway_instance_id", e.getKey()
                        ))
                        .toList();

                Map<String, Object> batchPayload = Map.of(
                        "entries", entries,
                        "message", Map.of(
                                "message_id", message.getMessageId(),
                                "conversation_id", message.getConversationId(),
                                "sender_id", message.getSenderId(),
                                "type", message.getType().name(),
                                "sequence_id", message.getSequenceId(),
                                "correlation_id", message.getCorrelationId() != null ? message.getCorrelationId() : ""
                        )
                );

                String json = objectMapper.writeValueAsString(batchPayload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + gatewayHost + ":" + gatewayPort + "/push/batch"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                log.info("GatewayPushClient: Batch pushed message {} to {} gateways, response: {}",
                        message.getMessageId(), entries.size(), response.statusCode());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return parseBatchResponse(response.body());
                } else {
                    return gatewayToConnectionIds.values().stream()
                            .flatMap(List::stream)
                            .map(id -> new PushResponse(false, "HTTP " + response.statusCode()))
                            .toList();
                }
            } catch (Exception e) {
                log.error("Failed to batch push to gateway: {}", e.getMessage(), e);
                return gatewayToConnectionIds.values().stream()
                        .flatMap(List::stream)
                        .map(id -> new PushResponse(false, e.getMessage()))
                        .toList();
            }
        });
    }

    private List<PushResponse> parseBatchResponse(String responseBody) throws Exception {
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        return results.stream()
                .map(r -> new PushResponse((Boolean) r.get("success"), null))
                .toList();
    }
}
