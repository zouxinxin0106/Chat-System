package com.chat.service.pipeline.stage;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.pipeline.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class AuthorizeStage implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(AuthorizeStage.class);

    @Override
    public ProcessedMessage process(ProcessedMessage msg) throws Exception {
        ChatMessage chatMessage = msg.getMessage();
        String correlationId = chatMessage != null ? chatMessage.getCorrelationId() : "unknown";

        log.info("[{}] AuthorizeStage: Authorizing message", correlationId);

        if (chatMessage == null) {
            log.warn("[{}] AuthorizeStage: No message to authorize", correlationId);
            msg.setAuthorized(false);
            msg.setAuthorizationError("Message is null");
            return msg;
        }

        // For demo purposes, always authorize
        // In production, this would check conversation membership, roles, etc.
        log.info("[{}] AuthorizeStage: Authorization granted for sender: {}", correlationId, chatMessage.getSenderId());
        msg.setAuthorized(true);

        return msg;
    }
}
