package com.chat.service.model;

public final class PresenceContent {
    private final String userId;
    private final PresenceStatus status;

    public enum PresenceStatus {
        ONLINE,
        OFFLINE,
        AWAY
    }

    public PresenceContent(String userId, PresenceStatus status) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.userId = userId;
        this.status = status;
    }

    public String userId() { return userId; }
    public PresenceStatus status() { return status; }
}