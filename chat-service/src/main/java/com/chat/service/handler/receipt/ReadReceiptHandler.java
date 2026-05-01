package com.chat.service.handler.receipt;

import com.chat.service.handler.DeliveryResult;
import com.chat.service.handler.MessageHandler;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.MessageType;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.model.ReadReceiptContent;
import com.chat.service.repository.MessageBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class ReadReceiptHandler implements MessageHandler {
    public static final MessageType MESSAGE_TYPE = MessageType.MESSAGE_TYPE_READ_RECEIPT;
    private static final Logger log = LoggerFactory.getLogger(ReadReceiptHandler.class);

    private final MessageBoxRepository messageBoxRepository;

    public ReadReceiptHandler(MessageBoxRepository messageBoxRepository) {
        this.messageBoxRepository = messageBoxRepository;
    }

    @Override
    public DeliveryResult handle(ProcessedMessage message) {
        ChatMessage chatMessage = message.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] ReadReceiptHandler: Processing read receipt", correlationId);

        try {
            if (chatMessage == null) {
                log.warn("[{}] ReadReceiptHandler: No message to process", correlationId);
                return DeliveryResult.failure("No message to process");
            }

            if (!(chatMessage.getContent() instanceof com.chat.service.model.ReadReceiptContent)) {
                log.warn("[{}] ReadReceiptHandler: Invalid content type for read receipt", correlationId);
                return DeliveryResult.failure("Invalid content type for read receipt");
            }
            com.chat.service.model.ReadReceiptContent receiptContent = (com.chat.service.model.ReadReceiptContent) chatMessage.getContent();

            String conversationId = receiptContent.conversationId();
            String lastReadMessageId = receiptContent.lastReadMessageId();
            String userId = chatMessage.getSenderId();

            log.info("[{}] ReadReceiptHandler: User {} read messages up to {} in conversation {}",
                    correlationId, userId, lastReadMessageId, conversationId);

            // Update is_read state
            messageBoxRepository.markAsRead(userId, lastReadMessageId);

            log.info("[{}] ReadReceiptHandler: Read receipt processed successfully", correlationId);
            return DeliveryResult.success(DeliveryStatus.DELIVERY_STATUS_READ);

        } catch (Exception e) {
            log.error("[{}] ReadReceiptHandler: Failed to process read receipt", correlationId, e);
            return DeliveryResult.failure("Failed to process read receipt: " + e.getMessage());
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
