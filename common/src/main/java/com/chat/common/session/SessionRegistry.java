package com.chat.common.session;

import com.chat.common.session.model.ConnectionInfo;

import java.util.List;
import java.util.Optional;

public interface SessionRegistry {

    void upsertSession(String userId, String connectionId, String gatewayInstanceId);

    void removeSession(String connectionId);

    void updatePresence(String userId, String status);

    String getPresence(String userId);

    List<ConnectionInfo> getUserSessions(String userId);

    Optional<ConnectionInfo> getSession(String connectionId);

    void start();

    void stop();
}
