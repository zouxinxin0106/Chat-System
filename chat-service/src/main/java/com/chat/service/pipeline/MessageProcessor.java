package com.chat.service.pipeline;

import com.chat.service.model.ProcessedMessage;

@FunctionalInterface
public interface MessageProcessor {
    ProcessedMessage process(ProcessedMessage msg) throws Exception;
}
