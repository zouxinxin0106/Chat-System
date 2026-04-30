package com.chat.gateway.pipeline;

import com.chat.gateway.grpc.GatewayPushClient;
import com.chat.gateway.metrics.GatewayMetrics;
import com.chat.proto.ChatMessage;
import com.chat.proto.SendMessageRequest;
import com.chat.proto.SendMessageResponse;
import com.chat.proto.DeliveryStatus;
import com.google.protobuf.Parser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);
    private static final Parser<ChatMessage> CHAT_MESSAGE_PARSER = ChatMessage.parser();

    private final GatewayPushClient pushClient;
    private final GatewayMetrics metrics;

    public ChatMessageHandler(GatewayPushClient pushClient, GatewayMetrics metrics) {
        this.pushClient = pushClient;
        this.metrics = metrics;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
            handleBinaryFrame(ctx, frame);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void handleBinaryFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        Timer.Sample timerSample = metrics.startGrpcPushTimer();
        
        try {
            byte[] data = new byte[frame.content().readableBytes()];
            frame.content().readBytes(data);
            
            ChatMessage chatMessage = CHAT_MESSAGE_PARSER.parseFrom(data);
            String userId = ctx.channel().attr(JwtAuthHandler.getUserIdKey()).get();
            String correlationId = chatMessage.getCorrelationId();
            
            logger.info("Received message: messageId={}, conversationId={}, senderId={}, correlationId={}, userId={}",
                    chatMessage.getMessageId(),
                    chatMessage.getConversationId(),
                    chatMessage.getSenderId(),
                    correlationId,
                    userId);

            SendMessageRequest request = SendMessageRequest.newBuilder()
                    .setMessage(chatMessage)
                    .build();

            SendMessageResponse response = pushClient.sendMessage(request);

            if (response != null && response.getStatus() == DeliveryStatus.DELIVERY_STATUS_SENT) {
                logger.info("Message forwarded successfully: messageId={}, correlationId={}, sequenceId={}",
                        chatMessage.getMessageId(),
                        correlationId,
                        response.getSequenceId());
            } else {
                logger.warn("Message forward returned non-success status: messageId={}, correlationId={}",
                        chatMessage.getMessageId(),
                        correlationId);
            }

            metrics.recordGrpcPushLatency(timerSample);
            sendAck(ctx, correlationId, response);

        } catch (Exception e) {
            metrics.incrementGrpcPushErrors();
            logger.error("Error processing message", e);
            ctx.fireExceptionCaught(e);
        } finally {
            frame.release();
        }
    }

    private void sendAck(ChannelHandlerContext ctx, String correlationId, SendMessageResponse response) {
        try {
            String ackMessage = "ACK:" + correlationId + ":" + (response != null ? response.getSequenceId() : 0);
            ctx.writeAndFlush(ackMessage);
        } catch (Exception e) {
            logger.error("Error sending ACK for correlationId: {}", correlationId, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in ChatMessageHandler for channel: {}", ctx.channel().id(), cause);
        ctx.close();
    }
}
