package com.chat.gateway;

import com.chat.gateway.grpc.GatewayPushClient;
import com.chat.gateway.heartbeat.HeartbeatManager;
import com.chat.gateway.metrics.GatewayMetrics;
import com.chat.gateway.pipeline.ChatMessageHandler;
import com.chat.gateway.pipeline.ChannelManager;
import com.chat.gateway.pipeline.HttpPushHandler;
import com.chat.gateway.pipeline.JwtAuthHandler;
import com.chat.gateway.push.PushHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class GatewayServer {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServer.class);

    private final int port;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final GatewayPushClient pushClient;
    private final GatewayMetrics metrics;
    private final HeartbeatManager heartbeatManager;
    private final ChannelManager channelManager;
    private final PushHandler pushHandler;
    private final AtomicBoolean started;
    private final String instanceId;

    public GatewayServer(int port, String instanceId) {
        this.port = port;
        this.instanceId = instanceId;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.pushClient = GatewayPushClient.createMock(instanceId);
        this.metrics = GatewayMetrics.getInstance();
        this.heartbeatManager = HeartbeatManager.getInstance();
        this.channelManager = ChannelManager.getInstance();
        this.pushHandler = new PushHandler(channelManager);
        this.started = new AtomicBoolean(false);
    }

    public void start() throws InterruptedException {
        if (started.getAndSet(true)) {
            logger.warn("GatewayServer already started");
            return;
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        pipeline.addLast("httpCodec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast("wsProtocol", new WebSocketServerProtocolHandler(
                                "/ws",
                                null,
                                true,
                                65536,
                                true));
                        pipeline.addLast("jwtAuth", new JwtAuthHandler());
                        pipeline.addLast("heartbeat", new HeartbeatManager.HeartbeatHandler(heartbeatManager));
                        pipeline.addLast("httpPush", new HttpPushHandler(pushHandler));
                        pipeline.addLast("chatMessage", new ChatMessageHandler(pushClient, metrics));
                        
                        ch.pipeline().addLast("connectionTracker", new ConnectionTrackerHandler());
                    }
                });

        Channel serverChannel = bootstrap.bind(port).sync().channel();
        logger.info("Gateway starting on port {}...", port);

        heartbeatManager.start(serverChannel);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void shutdown() {
        logger.info("Shutting down GatewayServer...");
        heartbeatManager.stop();
        pushClient.shutdown();

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        channelManager.clear();
        logger.info("GatewayServer shut down");
    }

    public boolean isStarted() {
        return started.get();
    }

    private class ConnectionTrackerHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
            metrics.incrementConnections();
            String userId = ctx.channel().attr(JwtAuthHandler.getUserIdKey()).get();
            if (userId != null) {
                channelManager.addChannel(userId, ctx.channel());
                logger.info("Channel active: userId={}, channelId={}", userId, ctx.channel().id());
            }
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
            metrics.decrementConnections();
            String userId = ctx.channel().attr(JwtAuthHandler.getUserIdKey()).get();
            if (userId != null) {
                channelManager.removeChannel(userId, ctx.channel());
                logger.info("Channel inactive: userId={}, channelId={}", userId, ctx.channel().id());
            }
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("Connection error on channel: {}", ctx.channel().id(), cause);
            ctx.close();
        }
    }
}
