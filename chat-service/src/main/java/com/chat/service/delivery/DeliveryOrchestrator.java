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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private static final int BATCH_SIZE = 100;

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
            var handlerOpt = handlerRegistry.getHandler(message.getType());
            if (handlerOpt.isEmpty()) {
                log.warn("[{}] DeliveryOrchestrator: No handler found for message type: {}", correlationId, message.getType());
                return DeliveryResult.failure("No handler for message type: " + message.getType());
            }

            var handlerResult = handlerOpt.get().handle(processedMessage);
            if (!handlerResult.success()) {
                log.warn("[{}] DeliveryOrchestrator: Handler failed: {}", correlationId, handlerResult.errorMessage());
                return handlerResult;
            }

            List<String> participantIds = conversationStore.getParticipantIds(message.getConversationId());
            if (participantIds.isEmpty()) {
                log.warn("[{}] DeliveryOrchestrator: No participants found for conversation: {}",
                        correlationId, message.getConversationId());
            }

            participantIds = participantIds.stream()
                    .filter(id -> !id.equals(message.getSenderId()))
                    .toList();

            log.info("[{}] DeliveryOrchestrator: Processing {} recipients", correlationId, participantIds.size());

            if (!participantIds.isEmpty()) {
                FanOutStrategy strategy = participantIds.size() >= READ_FAN_OUT_THRESHOLD
                        ? readFanOutStrategy
                        : writeFanOutStrategy;
                strategy.fanOut(message, participantIds);
            }

            for (String recipientId : participantIds) {
                List<ConnectionInfo> sessions = sessionRegistry.getUserSessions(recipientId);

                if (sessions.isEmpty()) {
                    log.info("[{}] DeliveryOrchestrator: User {} is offline, queuing message",
                            correlationId, recipientId);
                    offlineQueue.enqueue(new com.chat.service.queue.OfflineMessage(
                            message.getMessageId(),
                            recipientId,
                            null,
                            0,
                            Instant.now().plusSeconds(60)
                    ));
                } else {
                    for (ConnectionInfo session : sessions) {
                        gatewayPushClient.pushToConnection(
                                session.connectionId(),
                                session.gatewayInstanceId(),
                                message
                        ).thenAccept(response -> {
                            if (response.isSuccess()) {
                                log.info("[{}] DeliveryOrchestrator: Pushed to device for user {}",
                                        correlationId, recipientId);
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

    public List<DeliveryResult> orchestrateBatch(List<ProcessedMessage> processedMessages) {
        log.info("DeliveryOrchestrator: Starting batch orchestration for {} messages", processedMessages.size());

        if (processedMessages.isEmpty()) {
            return List.of();
        }

        List<DeliveryResult> results = new ArrayList<>();

        try {
            List<DeliveryResult> handlerResults = handlerRegistry.handleBatch(processedMessages);
            results.addAll(handlerResults);

            Map<String, List<String>> messageToRecipients = new HashMap<>();
            for (ProcessedMessage pm : processedMessages) {
                ChatMessage msg = pm.getMessage();
                List<String> participants = new ArrayList<>(conversationStore.getParticipantIds(msg.getConversationId()));
                participants.remove(msg.getSenderId());
                messageToRecipients.put(msg.getMessageId(), participants);
            }

            for (ProcessedMessage pm : processedMessages) {
                ChatMessage msg = pm.getMessage();
                List<String> recipients = messageToRecipients.get(msg.getMessageId());
                if (recipients != null && !recipients.isEmpty()) {
                    FanOutStrategy strategy = recipients.size() >= READ_FAN_OUT_THRESHOLD
                            ? readFanOutStrategy
                            : writeFanOutStrategy;
                    strategy.fanOut(msg, recipients);
                }
            }

            Map<String, List<ConnectionInfo>> sessionsByGateway = new HashMap<>();
            for (ProcessedMessage pm : processedMessages) {
                ChatMessage msg = pm.getMessage();
                List<String> recipients = messageToRecipients.get(msg.getMessageId());
                if (recipients == null) continue;

                for (String recipientId : recipients) {
                    List<ConnectionInfo> sessions = sessionRegistry.getUserSessions(recipientId);
                    for (ConnectionInfo session : sessions) {
                        sessionsByGateway
                                .computeIfAbsent(session.gatewayInstanceId(), k -> new ArrayList<>())
                                .add(session);
                    }
                }
            }

            for (Map.Entry<String, List<ConnectionInfo>> entry : sessionsByGateway.entrySet()) {
                String gatewayId = entry.getKey();
                List<ConnectionInfo> sessions = entry.getValue();

                List<List<String>> batches = partition(
                        sessions.stream().map(ConnectionInfo::connectionId).toList(),
                        BATCH_SIZE
                );

                for (List<String> batch : batches) {
                    ChatMessage message = processedMessages.get(0).getMessage();
                    gatewayPushClient.pushBatch(Map.of(gatewayId, batch), message);
                }
            }

            for (ProcessedMessage pm : processedMessages) {
                ChatMessage msg = pm.getMessage();
                List<String> recipients = messageToRecipients.get(msg.getMessageId());
                if (recipients == null) continue;

                for (String recipientId : recipients) {
                    List<ConnectionInfo> sessions = sessionRegistry.getUserSessions(recipientId);
                    if (sessions.isEmpty()) {
                        offlineQueue.enqueue(new com.chat.service.queue.OfflineMessage(
                                msg.getMessageId(),
                                recipientId,
                                null,
                                0,
                                Instant.now().plusSeconds(60)
                        ));
                    }
                }
            }

            log.info("DeliveryOrchestrator: Batch orchestration completed for {} messages", processedMessages.size());
        } catch (Exception e) {
            log.error("DeliveryOrchestrator: Batch orchestration failed", e);
        }

        return results;
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}