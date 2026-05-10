package com.chat.service.event;

import com.chat.common.event.EventBus;
import com.chat.service.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class KafkaEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public KafkaEventBus(KafkaTemplate<String, Object> kafkaTemplate, KafkaConfig kafkaConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public void publish(Object event) {
        String topic = determineTopic(event);
        String key = determineKey(event);

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}", event.getClass().getSimpleName(), topic, ex);
                    } else {
                        log.debug("Published event {} to topic {} partition {} offset {}",
                                event.getClass().getSimpleName(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    @Override
    public void subscribe(Class<?> eventType, Consumer<Object> handler) {
        log.info("KafkaEventBus does not support in-process subscription. Use @KafkaListener for consuming events.");
    }

    @Override
    public void unsubscribe(Class<?> eventType) {
        log.info("KafkaEventBus does not support in-process unsubscription.");
    }

    private String determineTopic(Object event) {
        String className = event.getClass().getSimpleName();
        if (className.contains("Presence")) {
            return kafkaConfig.getTopics().getPresenceEvents();
        }
        return kafkaConfig.getTopics().getMessageEvents();
    }

    private String determineKey(Object event) {
        return event.getClass().getSimpleName();
    }
}