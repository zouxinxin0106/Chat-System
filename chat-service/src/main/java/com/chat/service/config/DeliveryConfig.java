package com.chat.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "delivery")
public class DeliveryConfig {
    private FanoutConfig fanout = new FanoutConfig();
    private int batchSize = 100;

    public static class FanoutConfig {
        private int readThreshold = 100;
        private int writeThreshold = 100;

        public int getReadThreshold() { return readThreshold; }
        public void setReadThreshold(int readThreshold) { this.readThreshold = readThreshold; }
        public int getWriteThreshold() { return writeThreshold; }
        public void setWriteThreshold(int writeThreshold) { this.writeThreshold = writeThreshold; }
    }

    public FanoutConfig getFanout() { return fanout; }
    public void setFanout(FanoutConfig fanout) { this.fanout = fanout; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}