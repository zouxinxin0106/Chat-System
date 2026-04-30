package com.chat.gateway.grpc;

import com.chat.proto.ChatMessage;
import com.chat.proto.DeliveryStatus;
import com.chat.proto.PushRequest;
import com.chat.proto.PushResponse;
import com.chat.proto.SendMessageRequest;
import com.chat.proto.SendMessageResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class GatewayPushClient {

    private static final Logger logger = LoggerFactory.getLogger(GatewayPushClient.class);

    protected final ManagedChannel channel;
    protected final String instanceId;

    public GatewayPushClient(ManagedChannel channel, String instanceId) {
        this.channel = channel;
        this.instanceId = instanceId;
    }

    public abstract SendMessageResponse sendMessage(SendMessageRequest request);

    public abstract void pushToDevice(PushRequest request, StreamObserver<PushResponse> responseObserver);

    public void shutdown() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Shutdown interrupted", e);
        }
    }

    public static GatewayPushClient create(String host, int port, String instanceId) {
        return new DemoGatewayPushClient(instanceId);
    }

    public static GatewayPushClient createMock(String instanceId) {
        return new DemoGatewayPushClient(instanceId);
    }

    private static class DemoGatewayPushClient extends GatewayPushClient {

        public DemoGatewayPushClient(String instanceId) {
            super(null, instanceId);
        }

        @Override
        public SendMessageResponse sendMessage(SendMessageRequest request) {
            ChatMessage message = request.getMessage();
            logger.info("[DEMO gRPC] Would send message to ChatService: messageId={}, conversationId={}, senderId={}, correlationId={}",
                    message.getMessageId(),
                    message.getConversationId(),
                    message.getSenderId(),
                    message.getCorrelationId());

            return SendMessageResponse.newBuilder()
                    .setMessageId(message.getMessageId())
                    .setStatus(DeliveryStatus.DELIVERY_STATUS_SENT)
                    .setSequenceId(System.currentTimeMillis())
                    .build();
        }

        @Override
        public void pushToDevice(PushRequest request, StreamObserver<PushResponse> responseObserver) {
            logger.info("[DEMO gRPC] Would push to device: connectionId={}, gatewayInstanceId={}, messageId={}",
                    request.getConnectionId(),
                    request.getGatewayInstanceId(),
                    request.getMessage().getMessageId());

            responseObserver.onNext(PushResponse.newBuilder()
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();
        }
    }
}
