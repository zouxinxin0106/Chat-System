package com.chat.service.handler;

import com.chat.service.model.ProcessedMessage;
import java.util.List;

public interface MessageHandler {
    DeliveryResult handle(ProcessedMessage message);
    List<DeliveryResult> handleBatch(List<ProcessedMessage> messages);
}