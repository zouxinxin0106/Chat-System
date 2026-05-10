package com.chat.service.handler.patch;

import com.chat.common.event.EventBus;
import com.chat.service.event.PatchResultEvent;
import com.chat.service.handler.DeliveryResult;
import com.chat.service.handler.MessageHandler;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.MessageType;
import com.chat.service.model.PatchContent;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class PatchHandler implements MessageHandler {
    public static final MessageType MESSAGE_TYPE = MessageType.MESSAGE_TYPE_PATCH;
    private static final Logger log = LoggerFactory.getLogger(PatchHandler.class);

    private final MessageRepository messageRepository;
    private final EventBus eventBus;

    public PatchHandler(MessageRepository messageRepository, EventBus eventBus) {
        this.messageRepository = messageRepository;
        this.eventBus = eventBus;
    }

    @Override
    public DeliveryResult handle(ProcessedMessage message) {
        ChatMessage chatMessage = message.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] PatchHandler: Processing patch message", correlationId);

        try {
            if (chatMessage == null) {
                log.warn("[{}] PatchHandler: No message to process", correlationId);
                return DeliveryResult.failure("No message to process");
            }

            if (!(chatMessage.getContent() instanceof PatchContent)) {
                log.warn("[{}] PatchHandler: Invalid content type for patch", correlationId);
                return DeliveryResult.failure("Invalid content type for patch");
            }
            PatchContent patchContent = (PatchContent) chatMessage.getContent();

            String conversationId = patchContent.conversationId();
            long fromSequenceId = patchContent.fromSequenceId();
            long toSequenceId = patchContent.toSequenceId();
            String requesterId = chatMessage.getSenderId();

            log.info("[{}] PatchHandler: Fetching messages for conversation {} from seq {} to {}",
                    correlationId, conversationId, fromSequenceId, toSequenceId);

            List<ChatMessage> messages = messageRepository.getMessagesInRange(
                    conversationId, fromSequenceId, toSequenceId);

            log.info("[{}] PatchHandler: Found {} messages in range", correlationId, messages.size());

            eventBus.publish(new PatchResultEvent(conversationId, requesterId, messages));

            return DeliveryResult.success(DeliveryStatus.DELIVERY_STATUS_DELIVERED);

        } catch (Exception e) {
            log.error("[{}] PatchHandler: Failed to process patch", correlationId, e);
            return DeliveryResult.failure("Failed to process patch: " + e.getMessage());
        }
    }

    @Override
    public List<DeliveryResult> handleBatch(List<ProcessedMessage> messages) {
        List<DeliveryResult> results = new ArrayList<>();
        for (ProcessedMessage message : messages) {
            results.add(handle(message));
        }
        return results;
    }
}