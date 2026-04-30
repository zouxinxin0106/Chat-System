package com.chat.service.pipeline.stage;

import com.chat.common.id.SnowflakeIdGenerator;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.pipeline.MessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public final class EnrichStage implements MessageProcessor {
    private final SnowflakeIdGenerator idGenerator;
    private final AtomicLong sequenceCounter = new AtomicLong(0);

    @Autowired
    public EnrichStage(SnowflakeIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public SnowflakeIdGenerator getIdGenerator() { return idGenerator; }

    @Override
    public ProcessedMessage process(ProcessedMessage msg) throws Exception {
        ChatMessage chatMessage = msg.getMessage();
        String correlationId = (chatMessage != null && chatMessage.getCorrelationId() != null) ?
            chatMessage.getCorrelationId() : "unknown";

        if (chatMessage == null) {
            return msg;
        }

        if (chatMessage.getMessageId() == null || chatMessage.getMessageId().isBlank()) {
            chatMessage.setMessageId(String.valueOf(idGenerator.nextId()));
        }

        long sequenceId = sequenceCounter.incrementAndGet();
        chatMessage.setSequenceId(sequenceId);
        msg.setSequenceId(sequenceId);

        if (chatMessage.getCreateAt() == 0) {
            chatMessage.setCreateAt(Instant.now().toEpochMilli());
        }

        if (chatMessage.getStatus() == null) {
            chatMessage.setStatus(DeliveryStatus.DELIVERY_STATUS_SENT);
        }

        return msg;
    }
}
