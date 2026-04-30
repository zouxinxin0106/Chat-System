package com.chat.service.grpc;

import com.chat.common.id.SnowflakeIdGenerator;
import com.chat.common.tracing.CorrelationId;
import com.chat.service.delivery.DeliveryOrchestrator;
import com.chat.service.handler.HandlerRegistry;
import com.chat.service.logging.ServiceLogger;
import com.chat.service.model.*;
import com.chat.service.pipeline.MessagePipeline;
import com.chat.service.repository.inmemory.InMemoryMessageRepository;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class ChatServiceImpl {
    private static final ServiceLogger LOG = new ServiceLogger(ChatServiceImpl.class);
    private final MessagePipeline pipeline;
    private final HandlerRegistry handlerRegistry;
    private final DeliveryOrchestrator deliveryOrchestrator;
    private final InMemoryMessageRepository messageRepository;
    private final SnowflakeIdGenerator idGenerator;
    private static final Map<String, List<String>> CONVERSATION_PARTICIPANTS = new ConcurrentHashMap<>();

    @Autowired
    public ChatServiceImpl(
            MessagePipeline pipeline,
            HandlerRegistry handlerRegistry,
            DeliveryOrchestrator deliveryOrchestrator,
            InMemoryMessageRepository messageRepository,
            SnowflakeIdGenerator idGenerator) {
        this.pipeline = pipeline;
        this.handlerRegistry = handlerRegistry;
        this.deliveryOrchestrator = deliveryOrchestrator;
        this.messageRepository = messageRepository;
        this.idGenerator = idGenerator;
        CONVERSATION_PARTICIPANTS.put("conv1", List.of("userA", "userB"));
        CONVERSATION_PARTICIPANTS.put("conv2", List.of("userA", "userB", "userC"));
        LOG.info("ChatServiceImpl initialized");
    }

    public SendMessageResponse sendMessage(SendMessageRequest request) {
        ChatMessage inputMessage = request.getMessage();
        String correlationId = CorrelationId.generate();
        try {
            LOG.info("Processing message", Map.of(
                "correlation_id", correlationId,
                "conversation_id", inputMessage.getConversationId(),
                "sender_id", inputMessage.getSenderId(),
                "type", inputMessage.getType().name()
            ));
            ProcessedMessage processed = pipeline.process(inputMessage);
            var deliveryResult = handlerRegistry.getHandler(processed.getMessage().getType()).orElseThrow().handle(processed);
            LOG.info("Message processed", Map.of(
                "correlation_id", correlationId,
                "message_id", processed.getMessage().getMessageId(),
                "status", deliveryResult.deliveryStatus().name()
            ));
            return new SendMessageResponse(
                    processed.getMessage().getMessageId(),
                    deliveryResult.deliveryStatus(),
                    processed.getSequenceId()
            );
        } catch (Exception e) {
            LOG.error("Failed to process message", e);
            return new SendMessageResponse("", DeliveryStatus.DELIVERY_STATUS_SENT, 0);
        } finally {
            CorrelationId.clear();
        }
    }

    public GetMessagesResponse getMessages(GetMessagesRequest request) {
        List<ChatMessage> messages = messageRepository.getMessages(
                request.getConversationId(),
                request.getLimit() > 0 ? request.getLimit() : 50
        );
        return new GetMessagesResponse(messages);
    }

    public PushResponse pushToDevice(PushRequest request) {
        LOG.info("Push to device", Map.of(
            "connection_id", request.getConnectionId(),
            "message_id", request.getMessage().getMessageId()
        ));
        return new PushResponse(true, null);
    }

    public List<String> getParticipants(String conversationId) {
        return CONVERSATION_PARTICIPANTS.getOrDefault(conversationId, List.of());
    }
}
