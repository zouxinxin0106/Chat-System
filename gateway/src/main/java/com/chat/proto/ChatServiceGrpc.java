package com.chat.proto;

import io.grpc.stub.StreamObserver;

public abstract class ChatServiceGrpc {
    public static abstract class ChatServiceImplBase {
        public abstract void sendMessage(SendMessageRequest req, StreamObserver<SendMessageResponse> resp);
        public abstract void getMessages(GetMessagesRequest req, StreamObserver<GetMessagesResponse> resp);
        public abstract void pushToDevice(PushRequest req, StreamObserver<PushResponse> resp);
    }
}
