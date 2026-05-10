package com.chat.service.repository.mongodb;

import com.chat.service.model.ChatMessage;
import com.chat.service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoMessageRepository implements MessageRepository {
    private static final Logger log = LoggerFactory.getLogger(MongoMessageRepository.class);

    private final MongoTemplate mongoTemplate;

    @Override
    public ChatMessage save(ChatMessage message) {
        if (message == null || message.getMessageId() == null) {
            throw new IllegalArgumentException("Message and messageId cannot be null");
        }

        log.debug("Saving message {} to MongoDB for conversation {}",
                message.getMessageId(), message.getConversationId());

        mongoTemplate.save(message, "messages");
        log.info("Message {} saved successfully to MongoDB", message.getMessageId());
        return message;
    }

    @Override
    public List<ChatMessage> getMessages(String conversationId, int limit) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId))
                .limit(limit);
        return mongoTemplate.find(query, ChatMessage.class, "messages");
    }

    @Override
    public List<ChatMessage> getMessagesInRange(String conversationId, long fromSequenceId, long toSequenceId) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId)
                .and("sequenceId").gte(fromSequenceId).lte(toSequenceId));
        return mongoTemplate.find(query, ChatMessage.class, "messages");
    }

    @Override
    public Optional<ChatMessage> getMessage(String messageId) {
        Query query = new Query(Criteria.where("messageId").is(messageId));
        ChatMessage message = mongoTemplate.findOne(query, ChatMessage.class, "messages");
        return Optional.ofNullable(message);
    }
}