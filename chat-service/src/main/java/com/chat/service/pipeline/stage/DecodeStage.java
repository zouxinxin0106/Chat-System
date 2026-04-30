package com.chat.service.pipeline.stage;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.pipeline.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class DecodeStage implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(DecodeStage.class);

    @Override
    public ProcessedMessage process(ProcessedMessage msg) throws Exception {
        ChatMessage chatMessage = msg.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] DecodeStage: Decoding message", correlationId);

        if (chatMessage == null) {
            log.warn("[{}] DecodeStage: No message to decode", correlationId);
            msg.setValid(false);
            msg.setValidationError("No message to decode");
            return msg;
        }

        log.info("[{}] DecodeStage: Successfully decoded message for conversation: {}, sender: {}",
                correlationId, chatMessage.getConversationId(), chatMessage.getSenderId());

        return msg;
    }
}
