package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;
import com.chat.service.repository.MessageBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class WriteFanOutStrategy implements FanOutStrategy {
    private static final Logger log = LoggerFactory.getLogger(WriteFanOutStrategy.class);

    private final MessageBoxRepository messageBoxRepository;

    public WriteFanOutStrategy(MessageBoxRepository messageBoxRepository) {
        this.messageBoxRepository = messageBoxRepository;
    }

    @Override
    public void fanOut(ChatMessage message, List<String> recipientUserIds) {
        String correlationId = message.getCorrelationId() != null ? message.getCorrelationId() : "unknown";
        log.info("[{}] WriteFanOutStrategy: Writing to {} recipient message boxes", correlationId, recipientUserIds.size());

        for (String recipientId : recipientUserIds) {
            try {
                messageBoxRepository.saveEntry(recipientId, message.getMessageId());
                log.debug("[{}] WriteFanOutStrategy: Saved message {} for recipient {}", 
                        correlationId, message.getMessageId(), recipientId);
            } catch (Exception e) {
                log.warn("[{}] WriteFanOutStrategy: Failed to save entry for recipient {}: {}", 
                        correlationId, recipientId, e.getMessage());
            }
        }

        log.info("[{}] WriteFanOutStrategy: Fan-out completed for {} recipients", correlationId, recipientUserIds.size());
    }
}
