package com.chat.service.event;

import com.chat.service.config.KafkaConfig;
import com.chat.service.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public void publishMessageEvent(ChatMessage message) {
        kafkaTemplate.send(kafkaConfig.getTopics().getMessageEvents(), message.getMessageId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish message event for messageId {}", message.getMessageId(), ex);
                    } else {
                        log.debug("Published message event for messageId {} to partition {}",
                                message.getMessageId(), result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishPresenceEvent(String userId, String status) {
        Map<String, Object> presenceData = Map.of("userId", userId, "status", status);
        kafkaTemplate.send(kafkaConfig.getTopics().getPresenceEvents(), userId, presenceData)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish presence event for userId {}", userId, ex);
                    } else {
                        log.debug("Published presence event for userId {} to partition {}",
                                userId, result.getRecordMetadata().partition());
                    }
                });
    }
}