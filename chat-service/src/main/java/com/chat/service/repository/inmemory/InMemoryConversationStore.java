package com.chat.service.repository.inmemory;

import com.chat.service.repository.ConversationStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryConversationStore implements ConversationStore {

    private final Map<String, List<String>> conversationParticipants = new ConcurrentHashMap<>();

    public InMemoryConversationStore() {
        conversationParticipants.put("conv1", List.of("userA", "userB"));
        conversationParticipants.put("conv2", List.of("userA", "userB", "userC"));
    }

    @Override
    public List<String> getParticipantIds(String conversationId) {
        return conversationParticipants.getOrDefault(conversationId, List.of());
    }
}
