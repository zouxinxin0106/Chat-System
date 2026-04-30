package com.chat.service.pipeline;

import com.chat.common.logging.StructuredLogger;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.pipeline.stage.AuthorizeStage;
import com.chat.service.pipeline.stage.DecodeStage;
import com.chat.service.pipeline.stage.EnrichStage;
import com.chat.service.pipeline.stage.ValidateStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public final class MessagePipeline {
    private static final StructuredLogger LOG = StructuredLogger.getLogger(MessagePipeline.class);
    private final List<MessageProcessor> processors;

    @Autowired
    public MessagePipeline(DecodeStage decodeStage, ValidateStage validateStage, AuthorizeStage authorizeStage, EnrichStage enrichStage) {
        this.processors = List.of(decodeStage, validateStage, authorizeStage, enrichStage);
    }

    public ProcessedMessage process(ChatMessage inputMessage) throws Exception {
        ProcessedMessage current = ProcessedMessage.builder()
                .message(inputMessage)
                .correlationId(inputMessage.getCorrelationId() != null ?
                    inputMessage.getCorrelationId() : "unknown")
                .build();

        LOG.info("Starting pipeline", Map.of("correlation_id", current.getCorrelationId()));

        for (MessageProcessor processor : processors) {
            String stageName = processor.getClass().getSimpleName();
            LOG.info("Executing stage: " + stageName, Map.of("correlation_id", current.getCorrelationId()));
            current = processor.process(current);
        }

        LOG.info("Pipeline completed", Map.of(
            "correlation_id", current.getCorrelationId(),
            "message_id", current.getMessage().getMessageId(),
            "sequence_id", current.getSequenceId()
        ));
        return current;
    }
}
