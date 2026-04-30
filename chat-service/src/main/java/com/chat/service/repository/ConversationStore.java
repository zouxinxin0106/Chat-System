package com.chat.service.repository;

import java.util.List;

public interface ConversationStore {
    List<String> getParticipantIds(String conversationId);
}
