package com.chat.gateway.pipeline;

import com.chat.gateway.push.PushHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

public class HttpPushHandler extends ChannelInboundHandlerAdapter {

    private final PushHandler pushHandler;

    public HttpPushHandler(PushHandler pushHandler) {
        this.pushHandler = pushHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest request) {
            String uri = request.uri();
            if (uri.equals("/push") || uri.startsWith("/push/")) {
                pushHandler.handlePush(request, ctx);
                return;
            }
            if (uri.equals("/push/batch")) {
                pushHandler.handleBatchPush(request, ctx);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }
}