package com.chat.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {
    private String bootstrapServers = "localhost:9092";
    private String consumerGroupId = "chat-service";
    private Topics topics = new Topics();

    public static class Topics {
        private String messageEvents = "message-events";
        private String presenceEvents = "presence-events";

        public String getMessageEvents() { return messageEvents; }
        public void setMessageEvents(String messageEvents) { this.messageEvents = messageEvents; }
        public String getPresenceEvents() { return presenceEvents; }
        public void setPresenceEvents(String presenceEvents) { this.presenceEvents = presenceEvents; }
    }

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
    public String getConsumerGroupId() { return consumerGroupId; }
    public void setConsumerGroupId(String consumerGroupId) { this.consumerGroupId = consumerGroupId; }
    public Topics getTopics() { return topics; }
    public void setTopics(Topics topics) { this.topics = topics; }
}