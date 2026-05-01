package com.chat.service.repository;

import com.chat.service.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> getMessages(String conversationId, int limit);
    List<ChatMessage> getMessagesInRange(String conversationId, long fromSequenceId, long toSequenceId);
    Optional<ChatMessage> getMessage(String messageId);
}
