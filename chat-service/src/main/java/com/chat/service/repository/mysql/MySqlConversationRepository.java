package com.chat.service.repository.mysql;

import com.chat.service.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MySqlConversationRepository extends JpaRepository<ConversationEntity, String> {
    List<ConversationEntity> findByNameContaining(String name);
}