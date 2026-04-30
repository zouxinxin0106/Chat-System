package com.chat.service.repository.inmemory;

import com.chat.service.repository.MessageBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public final class InMemoryMessageBoxRepository implements MessageBoxRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageBoxRepository.class);

    private final ConcurrentHashMap<String, Set<String>> userMessages;

    public InMemoryMessageBoxRepository() {
        this.userMessages = new ConcurrentHashMap<>();
    }

    @Override
    public void saveEntry(String userId, String messageId) {
        if (userId == null || messageId == null) {
            throw new IllegalArgumentException("userId and messageId cannot be null");
        }

        userMessages.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(messageId);

        log.debug("Saved message {} for user {}", messageId, userId);
    }

    @Override
    public List<String> getUnread(String userId, String conversationId) {
        Set<String> messages = userMessages.get(userId);
        if (messages == null) {
            return Collections.emptyList();
        }

        // In a real implementation, we would filter by conversationId
        // For now, return all messages for the user
        return messages.stream().collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String userId, String messageId) {
        log.info("Marking message {} as read for user {}", messageId, userId);
        // In a real implementation, this would update the read status
        // For now, we just log the action
    }
}
