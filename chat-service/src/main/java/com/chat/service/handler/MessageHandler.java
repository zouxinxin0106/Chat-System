package com.chat.service.handler;

import com.chat.service.model.ProcessedMessage;

public interface MessageHandler {
    DeliveryResult handle(ProcessedMessage message);
}
