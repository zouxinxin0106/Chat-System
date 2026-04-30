package com.chat.gateway.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtAuthHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthHandler.class);
    private static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Skip token extraction for now - stub implementation
        String userId = "demo-user-" + ctx.channel().id();
        ctx.channel().attr(USER_ID_KEY).set(userId);
        logger.info("Channel authenticated for user: {}, channelId: {}", userId, ctx.channel().id());
        ctx.fireChannelActive();
    }

    public static AttributeKey<String> getUserIdKey() {
        return USER_ID_KEY;
    }
}
