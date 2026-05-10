package com.chat.service.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations")
public class ConversationEntity {

    @Id
    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "updated_at")
    private long updatedAt;

    public ConversationEntity() {}

    public ConversationEntity(String conversationId, String name, long createdAt, long updatedAt) {
        this.conversationId = conversationId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}