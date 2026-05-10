package com.chat.service.handler.presence;

import com.chat.service.handler.DeliveryResult;
import com.chat.service.handler.MessageHandler;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.MessageType;
import com.chat.service.model.PresenceContent;
import com.chat.service.model.ProcessedMessage;
import com.chat.common.session.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class PresenceHandler implements MessageHandler {
    public static final MessageType MESSAGE_TYPE = MessageType.MESSAGE_TYPE_PRESENCE;
    private static final Logger log = LoggerFactory.getLogger(PresenceHandler.class);

    private final SessionRegistry sessionRegistry;

    public PresenceHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public DeliveryResult handle(ProcessedMessage message) {
        ChatMessage chatMessage = message.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] PresenceHandler: Processing presence message", correlationId);

        try {
            if (chatMessage == null) {
                log.warn("[{}] PresenceHandler: No message to process", correlationId);
                return DeliveryResult.failure("No message to process");
            }

            if (!(chatMessage.getContent() instanceof PresenceContent)) {
                log.warn("[{}] PresenceHandler: Invalid content type for presence", correlationId);
                return DeliveryResult.failure("Invalid content type for presence");
            }
            PresenceContent presenceContent = (PresenceContent) chatMessage.getContent();

            String userId = presenceContent.userId();
            String status = presenceContent.status().name();

            log.info("[{}] PresenceHandler: Updating presence for user {} to {}", correlationId, userId, status);
            sessionRegistry.updatePresence(userId, status);

            return DeliveryResult.success(DeliveryStatus.DELIVERY_STATUS_READ);

        } catch (Exception e) {
            log.error("[{}] PresenceHandler: Failed to process presence", correlationId, e);
            return DeliveryResult.failure("Failed to process presence: " + e.getMessage());
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