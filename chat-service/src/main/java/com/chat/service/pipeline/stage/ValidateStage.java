package com.chat.service.pipeline.stage;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.pipeline.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class ValidateStage implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(ValidateStage.class);

    @Override
    public ProcessedMessage process(ProcessedMessage msg) throws Exception {
        ChatMessage chatMessage = msg.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] ValidateStage: Validating message", correlationId);

        if (chatMessage == null) {
            log.warn("[{}] ValidateStage: No message to validate", correlationId);
            msg.setValid(false);
            msg.setValidationError("Message is null");
            return msg;
        }

        if (chatMessage.getConversationId() == null || chatMessage.getConversationId().isBlank()) {
            log.warn("[{}] ValidateStage: Missing conversationId", correlationId);
            msg.setValid(false);
            msg.setValidationError("conversationId is required");
            return msg;
        }

        if (chatMessage.getSenderId() == null || chatMessage.getSenderId().isBlank()) {
            log.warn("[{}] ValidateStage: Missing senderId", correlationId);
            msg.setValid(false);
            msg.setValidationError("senderId is required");
            return msg;
        }

        if (chatMessage.getType() == null) {
            log.warn("[{}] ValidateStage: Missing message type", correlationId);
            msg.setValid(false);
            msg.setValidationError("message type is required");
            return msg;
        }

        log.info("[{}] ValidateStage: Message validated successfully", correlationId);
        return msg;
    }
}
