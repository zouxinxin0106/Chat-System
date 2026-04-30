package com.chat.service.delivery;

import com.chat.common.event.EventBus;
import com.chat.service.event.MessageDeliveredEvent;
import com.chat.service.handler.DeliveryResult;
import com.chat.service.handler.HandlerRegistry;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.queue.OfflineQueue;
import com.chat.service.repository.ConversationStore;
import com.chat.service.repository.MessageRepository;
import com.chat.common.session.SessionRegistry;
import com.chat.common.session.model.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public final class DeliveryOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(DeliveryOrchestrator.class);

    private final MessageRepository messageRepository;
    private final ConversationStore conversationStore;
    private final SessionRegistry sessionRegistry;
    private final HandlerRegistry handlerRegistry;
    private final GatewayPushClient gatewayPushClient;
    private final OfflineQueue offlineQueue;
    private final EventBus eventBus;
    private final WriteFanOutStrategy writeFanOutStrategy;
    private final ReadFanOutStrategy readFanOutStrategy;

    private static final int READ_FAN_OUT_THRESHOLD = 100;

    @Autowired
    public DeliveryOrchestrator(
            MessageRepository messageRepository,
            ConversationStore conversationStore,
            SessionRegistry sessionRegistry,
            HandlerRegistry handlerRegistry,
            GatewayPushClient gatewayPushClient,
            OfflineQueue offlineQueue,
            EventBus eventBus,
            WriteFanOutStrategy writeFanOutStrategy,
            ReadFanOutStrategy readFanOutStrategy) {
        this.messageRepository = messageRepository;
        this.conversationStore = conversationStore;
        this.sessionRegistry = sessionRegistry;
        this.handlerRegistry = handlerRegistry;
        this.gatewayPushClient = gatewayPushClient;
        this.offlineQueue = offlineQueue;
        this.eventBus = eventBus;
        this.writeFanOutStrategy = writeFanOutStrategy;
        this.readFanOutStrategy = readFanOutStrategy;
    }

    public DeliveryResult orchestrate(ProcessedMessage processedMessage) {
        ChatMessage message = processedMessage.getMessage();
        String correlationId = message != null ? message.getCorrelationId() : "unknown";

        log.info("[{}] DeliveryOrchestrator: Starting delivery orchestration", correlationId);

        try {
            // Step 1: Get handler and process message
            var handlerOpt = handlerRegistry.getHandler(message.getType());
            if (handlerOpt.isEmpty()) {
                log.warn("[{}] DeliveryOrchestrator: No handler found for message type: {}", correlationId, message.getType());
                return DeliveryResult.failure("No handler for message type: " + message.getType());
            }

            // Step 2: Handle via specific handler (persist, etc.)
            var handlerResult = handlerOpt.get().handle(processedMessage);
            if (!handlerResult.success()) {
                log.warn("[{}] DeliveryOrchestrator: Handler failed: {}", correlationId, handlerResult.errorMessage());
                return handlerResult;
            }

            // Step 3: Get recipients from conversation
            List<String> participantIds = conversationStore.getParticipantIds(message.getConversationId());
            if (participantIds.isEmpty()) {
                log.warn("[{}] DeliveryOrchestrator: No participants found for conversation: {}", 
                        correlationId, message.getConversationId());
            }

            // Remove sender from recipients
            participantIds = participantIds.stream()
                    .filter(id -> !id.equals(message.getSenderId()))
                    .toList();

            log.info("[{}] DeliveryOrchestrator: Processing {} recipients", correlationId, participantIds.size());

            // Step 4: Fan-out to recipients based on group size
            if (!participantIds.isEmpty()) {
                FanOutStrategy strategy = participantIds.size() >= READ_FAN_OUT_THRESHOLD
                        ? readFanOutStrategy
                        : writeFanOutStrategy;
                strategy.fanOut(message, participantIds);
            }

            // Step 5: Query sessions and push to online devices
            for (String recipientId : participantIds) {
                List<ConnectionInfo> sessions = sessionRegistry.getUserSessions(recipientId);
                
                if (sessions.isEmpty()) {
                    log.info("[{}] DeliveryOrchestrator: User {} is offline, queuing message", 
                            correlationId, recipientId);
                    // Queue for offline delivery
                    offlineQueue.enqueue(new com.chat.service.queue.OfflineMessage(
                            message.getMessageId(),
                            recipientId,
                            null,
                            0,
                            Instant.now().plusSeconds(60)
                    ));
                } else {
                    // Push to each session
                    for (ConnectionInfo session : sessions) {
                        gatewayPushClient.pushToConnection(
                                session.connectionId(),
                                session.gatewayInstanceId(),
                                message
                        ).thenAccept(response -> {
                            if (response.isSuccess()) {
                                log.info("[{}] DeliveryOrchestrator: Pushed to device for user {}", 
                                        correlationId, recipientId);
                                // Publish delivered event
                                eventBus.publish(new MessageDeliveredEvent(
                                        message.getMessageId(),
                                        recipientId,
                                        Instant.now().toEpochMilli()
                                ));
                            } else {
                                log.warn("[{}] DeliveryOrchestrator: Push failed for user {}: {}", 
                                        correlationId, recipientId, response.getError());
                            }
                        });
                    }
                }
            }

            log.info("[{}] DeliveryOrchestrator: Delivery orchestration completed", correlationId);
            return handlerResult;

        } catch (Exception e) {
            log.error("[{}] DeliveryOrchestrator: Orchestration failed", correlationId, e);
            return DeliveryResult.failure("Delivery orchestration failed: " + e.getMessage());
        }
    }
}
