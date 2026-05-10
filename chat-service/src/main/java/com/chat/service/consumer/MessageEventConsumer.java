package com.chat.service.consumer;

import com.chat.service.config.KafkaConfig;
import com.chat.service.delivery.DeliveryOrchestrator;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(MessageEventConsumer.class);

    private final DeliveryOrchestrator deliveryOrchestrator;

    @KafkaListener(topics = "#{kafkaConfig.topics.messageEvents}", groupId = "#{kafkaConfig.consumerGroupId}")
    public void consumeMessageEvent(ChatMessage message) {
        log.info("Consuming message event for messageId: {}", message.getMessageId());
        try {
            ProcessedMessage pm = ProcessedMessage.builder().message(message).build();
            deliveryOrchestrator.orchestrate(pm);
        } catch (Exception e) {
            log.error("Failed to process message event for messageId: {}", message.getMessageId(), e);
        }
    }
}