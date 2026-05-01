package com.chat.service.pipeline;

import com.chat.service.model.ChatMessage;
import com.chat.service.model.ProcessedMessage;

@FunctionalInterface
public interface MessageProcessor {
    ProcessedMessage process(ProcessedMessage msg) throws Exception;

    default java.util.List<ProcessedMessage> processBatch(java.util.List<ProcessedMessage> messages) throws Exception {
        java.util.List<ProcessedMessage> results = new java.util.ArrayList<>();
        for (ProcessedMessage msg : messages) {
            results.add(process(msg));
        }
        return results;
    }
}
