package com.chat.service.repository;

import java.util.List;

public interface MessageBoxRepository {
    void saveEntry(String userId, String messageId);
    List<String> getUnread(String userId, String conversationId);
    void markAsRead(String userId, String messageId);
}
