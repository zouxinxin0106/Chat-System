package com.chat.service.repository;

import com.chat.service.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> getMessages(String conversationId, int limit);
    Optional<ChatMessage> getMessage(String messageId);
}
