package com.chat.gateway.heartbeat;

import com.chat.gateway.metrics.GatewayMetrics;
import com.chat.gateway.pipeline.ChannelManager;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HeartbeatManager {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatManager.class);
    private static final long PING_INTERVAL_SECONDS = 30;
    private static final int MAX_MISSED_PONGS = 2;

    private final Map<Channel, PingState> channelPings;
    private final ChannelManager channelManager;
    private final GatewayMetrics metrics;
    private ScheduledFuture<?> pingTask;

    private static volatile HeartbeatManager instance;

    public static class PingState {
        private int missedPongs;
        private long lastPingTime;

        public PingState() {
            this.missedPongs = 0;
            this.lastPingTime = System.currentTimeMillis();
        }

        public void incrementMissedPongs() {
            this.missedPongs++;
        }

        public int getMissedPongs() {
            return missedPongs;
        }

        public void resetMissedPongs() {
            this.missedPongs = 0;
        }

        public void updateLastPingTime() {
            this.lastPingTime = System.currentTimeMillis();
        }

        public long getLastPingTime() {
            return lastPingTime;
        }
    }

    private HeartbeatManager(ChannelManager channelManager, GatewayMetrics metrics) {
        this.channelManager = channelManager;
        this.metrics = metrics;
        this.channelPings = new ConcurrentHashMap<>();
    }

    public static HeartbeatManager getInstance() {
        if (instance == null) {
            synchronized (HeartbeatManager.class) {
                if (instance == null) {
                    instance = new HeartbeatManager(ChannelManager.getInstance(), GatewayMetrics.getInstance());
                }
            }
        }
        return instance;
    }

    public void start(Channel eventLoopChannel) {
        if (pingTask != null && !pingTask.isCancelled()) {
            return;
        }

        pingTask = eventLoopChannel.eventLoop().scheduleAtFixedRate(
                this::sendPingsAndCheckTimeouts,
                PING_INTERVAL_SECONDS,
                PING_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
        logger.info("HeartbeatManager started with {} second interval", PING_INTERVAL_SECONDS);
    }

    public void stop() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
        channelPings.clear();
        logger.info("HeartbeatManager stopped");
    }

    private void sendPingsAndCheckTimeouts() {
        long currentTime = System.currentTimeMillis();
        long pingIntervalMillis = PING_INTERVAL_SECONDS * 1000;

        Iterator<Map.Entry<Channel, PingState>> iterator = channelPings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Channel, PingState> entry = iterator.next();
            Channel channel = entry.getKey();
            PingState state = entry.getValue();

            if (!channel.isActive()) {
                iterator.remove();
                continue;
            }

            if (currentTime - state.getLastPingTime() >= pingIntervalMillis) {
                PingWebSocketFrame pingFrame = new PingWebSocketFrame();
                channel.writeAndFlush(pingFrame);
                state.updateLastPingTime();
                logger.debug("Sent ping to channel: {}", channel.id());
            }

            long timeSinceLastPing = currentTime - state.getLastPingTime();
            if (timeSinceLastPing > pingIntervalMillis * (MAX_MISSED_PONGS + 1)) {
                logger.warn("Connection marked as dead due to missed pongs: {}", channel.id());
                metrics.incrementHeartbeatTimeouts();
                channel.close();
                iterator.remove();
            }
        }
    }

    public void onPongReceived(Channel channel) {
        PingState state = channelPings.get(channel);
        if (state != null) {
            state.resetMissedPongs();
            logger.debug("Pong received from channel: {}", channel.id());
        }
    }

    public void registerChannel(Channel channel) {
        channelPings.put(channel, new PingState());
    }

    public void unregisterChannel(Channel channel) {
        channelPings.remove(channel);
    }

    public int getActiveChannelCount() {
        return channelPings.size();
    }

    public static class HeartbeatHandler extends ChannelInboundHandlerAdapter {
        private final HeartbeatManager heartbeatManager;

        public HeartbeatHandler(HeartbeatManager heartbeatManager) {
            this.heartbeatManager = heartbeatManager;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            heartbeatManager.registerChannel(ctx.channel());
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            heartbeatManager.unregisterChannel(ctx.channel());
            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof PongWebSocketFrame) {
                heartbeatManager.onPongReceived(ctx.channel());
            } else {
                super.channelRead(ctx, msg);
            }
        }
    }
}
