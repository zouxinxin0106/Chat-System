package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class ReadFanOutStrategy implements FanOutStrategy {
    private static final Logger log = LoggerFactory.getLogger(ReadFanOutStrategy.class);

    @Override
    public void fanOut(ChatMessage message, List<String> recipientUserIds) {
        String correlationId = message.getCorrelationId() != null ? message.getCorrelationId() : "unknown";
        log.info("[{}] ReadFanOutStrategy: Large group ({} >= 100 members), skipping mailbox write. Recipients: {}",
                correlationId, recipientUserIds.size(), recipientUserIds.size());

        for (String recipientId : recipientUserIds) {
            log.debug("[{}] ReadFanOutStrategy: Recipient {} can read via pull", correlationId, recipientId);
        }

        log.info("[{}] ReadFanOutStrategy: Fan-out completed for {} recipients (read-optimized)", correlationId, recipientUserIds.size());
    }
}