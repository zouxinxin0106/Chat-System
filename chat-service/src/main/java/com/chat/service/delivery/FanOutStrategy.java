package com.chat.service.delivery;

import com.chat.service.model.ChatMessage;

import java.util.List;

public interface FanOutStrategy {
    void fanOut(ChatMessage message, List<String> recipientUserIds);
}
