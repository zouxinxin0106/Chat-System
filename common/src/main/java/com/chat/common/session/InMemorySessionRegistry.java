package com.chat.common.session;

import com.chat.common.session.model.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class InMemorySessionRegistry implements SessionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(InMemorySessionRegistry.class);
    private static final long DEFAULT_TTL_SECONDS = 60;
    private static final long CLEANUP_INTERVAL_SECONDS = 10;

    private final Map<String, Map<String, ConnectionInfo>> sessionsByUser = new ConcurrentHashMap<>();
    private final long ttlSeconds;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = false;

    public InMemorySessionRegistry() {
        this(DEFAULT_TTL_SECONDS);
    }

    public InMemorySessionRegistry(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "session-registry-cleanup");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void upsertSession(String userId, String connectionId, String gatewayInstanceId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);

        ConnectionInfo connectionInfo = new ConnectionInfo(
            connectionId,
            gatewayInstanceId,
            userId,
            now,
            expiresAt
        );

        sessionsByUser
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(connectionId, connectionInfo);

        logger.debug("Upserted session for user {} with connection {}, expires at {}",
            userId, connectionId, expiresAt);
    }

    @Override
    public void removeSession(String connectionId) {
        for (Map<String, ConnectionInfo> userSessions : sessionsByUser.values()) {
            ConnectionInfo removed = userSessions.remove(connectionId);
            if (removed != null) {
                logger.debug("Removed session {} for user {}", connectionId, removed.userId());
                return;
            }
        }
        logger.debug("Session {} not found for removal", connectionId);
    }

    @Override
    public List<ConnectionInfo> getUserSessions(String userId) {
        Map<String, ConnectionInfo> userSessions = sessionsByUser.get(userId);
        if (userSessions == null) {
            return List.of();
        }

        Instant now = Instant.now();
        List<ConnectionInfo> activeSessions = new ArrayList<>();

        List<String> expiredConnectionIds = new ArrayList<>();

        for (Map.Entry<String, ConnectionInfo> entry : userSessions.entrySet()) {
            ConnectionInfo info = entry.getValue();
            if (info.expiresAt().isBefore(now)) {
                expiredConnectionIds.add(entry.getKey());
            } else {
                activeSessions.add(info);
            }
        }

        for (String connectionId : expiredConnectionIds) {
            userSessions.remove(connectionId);
            logger.debug("Cleaned up expired session {}", connectionId);
        }

        return List.copyOf(activeSessions);
    }

    @Override
    public Optional<ConnectionInfo> getSession(String connectionId) {
        Instant now = Instant.now();

        for (Map<String, ConnectionInfo> userSessions : sessionsByUser.values()) {
            ConnectionInfo info = userSessions.get(connectionId);
            if (info != null) {
                if (info.expiresAt().isBefore(now)) {
                    userSessions.remove(connectionId);
                    logger.debug("Session {} expired, removing", connectionId);
                    return Optional.empty();
                }
                return Optional.of(info);
            }
        }

        return Optional.empty();
    }

    @Override
    public void start() {
        if (running) {
            logger.warn("SessionRegistry already started");
            return;
        }

        running = true;

        scheduler.scheduleAtFixedRate(
            this::cleanupExpiredSessions,
            CLEANUP_INTERVAL_SECONDS,
            CLEANUP_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );

        logger.info("InMemorySessionRegistry started with TTL={}s, cleanup interval={}s",
            ttlSeconds, CLEANUP_INTERVAL_SECONDS);
    }

    @Override
    public void stop() {
        if (!running) {
            logger.warn("SessionRegistry already stopped");
            return;
        }

        running = false;

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        sessionsByUser.clear();

        logger.info("InMemorySessionRegistry stopped");
    }

    private void cleanupExpiredSessions() {
        if (!running) {
            return;
        }

        Instant now = Instant.now();
        int cleanedCount = 0;

        for (Map<String, ConnectionInfo> userSessions : sessionsByUser.values()) {
            List<String> expiredIds = new ArrayList<>();

            for (Map.Entry<String, ConnectionInfo> entry : userSessions.entrySet()) {
                if (entry.getValue().expiresAt().isBefore(now)) {
                    expiredIds.add(entry.getKey());
                }
            }

            for (String connectionId : expiredIds) {
                ConnectionInfo removed = userSessions.remove(connectionId);
                if (removed != null) {
                    cleanedCount++;
                    logger.debug("Cleaned up expired session {} for user {}",
                        connectionId, removed.userId());
                }
            }
        }

        if (cleanedCount > 0) {
            logger.info("Cleaned up {} expired sessions", cleanedCount);
        }
    }
}
