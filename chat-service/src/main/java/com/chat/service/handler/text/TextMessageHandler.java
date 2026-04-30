package com.chat.service.handler.text;

import com.chat.service.handler.DeliveryResult;
import com.chat.service.handler.MessageHandler;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.MessageType;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class TextMessageHandler implements MessageHandler {
    public static final MessageType MESSAGE_TYPE = MessageType.MESSAGE_TYPE_TEXT;
    private static final Logger log = LoggerFactory.getLogger(TextMessageHandler.class);

    private final MessageRepository messageRepository;

    public TextMessageHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public DeliveryResult handle(ProcessedMessage message) {
        ChatMessage chatMessage = message.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] TextMessageHandler: Processing text message", correlationId);

        try {
            if (chatMessage == null) {
                log.warn("[{}] TextMessageHandler: No message to process", correlationId);
                return DeliveryResult.failure("No message to process");
            }

            // Persist message
            ChatMessage saved = messageRepository.save(chatMessage);
            log.info("[{}] TextMessageHandler: Message persisted with id: {}", correlationId, saved.getMessageId());

            // Log delivery action
            log.info("[{}] TextMessageHandler: Message delivered to conversation: {}, sender: {}",
                    correlationId, saved.getConversationId(), saved.getSenderId());

            return DeliveryResult.success(saved.getStatus() != null ? saved.getStatus() : DeliveryStatus.DELIVERY_STATUS_SENT);

        } catch (Exception e) {
            log.error("[{}] TextMessageHandler: Failed to process message", correlationId, e);
            return DeliveryResult.failure("Failed to process message: " + e.getMessage());
        }
    }
}
