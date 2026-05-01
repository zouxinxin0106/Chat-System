package com.chat.service.repository.inmemory;

import com.chat.service.model.ChatMessage;
import com.chat.service.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public final class InMemoryMessageRepository implements MessageRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageRepository.class);

    private final ConcurrentHashMap<String, List<ChatMessage>> conversationMessages;
    private final ConcurrentHashMap<String, ChatMessage> messageById;
    private final ConcurrentHashMap<String, AtomicLong> sequenceCounters;

    public InMemoryMessageRepository() {
        this.conversationMessages = new ConcurrentHashMap<>();
        this.messageById = new ConcurrentHashMap<>();
        this.sequenceCounters = new ConcurrentHashMap<>();
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        if (message == null || message.getMessageId() == null) {
            throw new IllegalArgumentException("Message and messageId cannot be null");
        }

        String conversationId = message.getConversationId();
        String messageId = message.getMessageId();

        log.debug("Saving message {} to conversation {}", messageId, conversationId);

        // Store by message ID
        messageById.put(messageId, message);

        // Store in conversation list
        conversationMessages.computeIfAbsent(conversationId, k -> new ArrayList<>())
                .add(message);

        // Update sequence counter
        AtomicLong counter = sequenceCounters.computeIfAbsent(conversationId, k -> new AtomicLong(0));
        if (message.getSequenceId() > counter.get()) {
            counter.set(message.getSequenceId());
        }

        log.info("Message {} saved successfully to conversation {}", messageId, conversationId);
        return message;
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId, int limit) {
        List<ChatMessage> messages = conversationMessages.getOrDefault(conversationId, new ArrayList<>());
        
        return messages.stream()
                .sorted(Comparator.comparingLong(ChatMessage::getSequenceId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChatMessage> getMessage(String messageId) {
        return Optional.ofNullable(messageById.get(messageId));
    }

    @Override
    public List<ChatMessage> getMessagesInRange(String conversationId, long fromSequenceId, long toSequenceId) {
        List<ChatMessage> messages = conversationMessages.getOrDefault(conversationId, new ArrayList<>());

        return messages.stream()
                .filter(m -> m.getSequenceId() >= fromSequenceId && m.getSequenceId() <= toSequenceId)
                .sorted(Comparator.comparingLong(ChatMessage::getSequenceId))
                .collect(Collectors.toList());
    }

    public long getNextSequenceId(String conversationId) {
        return sequenceCounters.computeIfAbsent(conversationId, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    public int getTotalMessageCount() {
        return messageById.size();
    }
}
