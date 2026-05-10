package com.chat.gateway.push;

import com.chat.gateway.pipeline.ChannelManager;
import com.chat.proto.ChatMessage;
import com.chat.proto.PushResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PushHandler {

    private static final Logger logger = LoggerFactory.getLogger(PushHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ChannelManager channelManager;

    public PushHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public void handlePush(HttpRequest request, ChannelHandlerContext ctx) {
        if (!request.method().equals(HttpMethod.POST)) {
            sendResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String uri = request.uri();
        if (!uri.equals("/push") && !uri.startsWith("/push/")) {
            sendResponse(ctx, HttpResponseStatus.NOT_FOUND, "{\"error\":\"Not found\"}");
            return;
        }

        try {
            if (request instanceof FullHttpRequest fullRequest) {
                processPushRequest(fullRequest, ctx);
            } else {
                sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{\"error\":\"Expected FullHttpRequest\"}");
            }
        } catch (Exception e) {
            logger.error("Error handling push request", e);
            sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @SuppressWarnings("unchecked")
    private void processPushRequest(FullHttpRequest request, ChannelHandlerContext ctx) throws Exception {
        ByteBuf content = request.content();
        String json = content.toString(CharsetUtil.UTF_8);

        Map<String, Object> pushData = objectMapper.readValue(json, Map.class);
        String connectionId = (String) pushData.get("connection_id");
        String gatewayInstanceId = (String) pushData.get("gateway_instance_id");
        Map<String, Object> messageData = (Map<String, Object>) pushData.get("message");

        logger.info("Push to connection {}: messageId={}, gatewayInstanceId={}",
                connectionId, messageData.get("message_id"), gatewayInstanceId);

        List<Channel> channels = channelManager.getChannels(connectionId);
        boolean success = true;
        if (channels.isEmpty()) {
            logger.info("No channels found for connectionId: {}", connectionId);
        } else {
            for (Channel channel : channels) {
                if (channel.isActive()) {
                    String msgJson = objectMapper.writeValueAsString(messageData);
                    channel.writeAndFlush(msgJson);
                    logger.info("Pushed message {} to channel {}", messageData.get("message_id"), channel.id());
                }
            }
        }
        sendPushResponse(ctx, success, null);
    }

    private void sendPushResponse(ChannelHandlerContext ctx, boolean success, String error) {
        String json = "{\"success\":" + success + ",\"error\":" + (error != null ? "\"" + error + "\"" : "null") + "}";
        sendResponse(ctx, HttpResponseStatus.OK, json);
    }

    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(body, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    @SuppressWarnings("unchecked")
    public void handleBatchPush(HttpRequest request, ChannelHandlerContext ctx) {
        if (!request.method().equals(HttpMethod.POST)) {
            sendResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            if (request instanceof FullHttpRequest fullRequest) {
                processBatchPushRequest(fullRequest, ctx);
            } else {
                sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{\"error\":\"Expected FullHttpRequest\"}");
            }
        } catch (Exception e) {
            logger.error("Error handling batch push request", e);
            sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @SuppressWarnings("unchecked")
    private void processBatchPushRequest(FullHttpRequest request, ChannelHandlerContext ctx) throws Exception {
        ByteBuf content = request.content();
        String json = content.toString(CharsetUtil.UTF_8);

        Map<String, Object> batchData = objectMapper.readValue(json, Map.class);
        List<Map<String, Object>> entries = (List<Map<String, Object>>) batchData.get("entries");
        Map<String, Object> messageData = (Map<String, Object>) batchData.get("message");

        logger.info("Batch push to {} entries: messageId={}", entries.size(), messageData.get("message_id"));

        List<Map<String, Object>> results = new java.util.ArrayList<>();
        String msgJson = objectMapper.writeValueAsString(messageData);

        for (Map<String, Object> entry : entries) {
            List<String> connectionIds = (List<String>) entry.get("connection_ids");
            String gatewayInstanceId = (String) entry.get("gateway_instance_id");

            for (String connectionId : connectionIds) {
                List<Channel> channels = channelManager.getChannels(connectionId);
                boolean success = false;
                if (!channels.isEmpty()) {
                    for (Channel channel : channels) {
                        if (channel.isActive()) {
                            channel.writeAndFlush(msgJson);
                            success = true;
                            logger.debug("Batch pushed message {} to channel {}", messageData.get("message_id"), channel.id());
                        }
                    }
                }
                results.add(Map.of("connection_id", connectionId, "success", success));
            }
        }

        String responseJson = objectMapper.writeValueAsString(Map.of("success", true, "results", results));
        sendResponse(ctx, HttpResponseStatus.OK, responseJson);
    }
}