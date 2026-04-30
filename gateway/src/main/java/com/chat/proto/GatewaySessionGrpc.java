package com.chat.proto;

import io.grpc.stub.StreamObserver;

public abstract class GatewaySessionGrpc {
    public static abstract class GatewaySessionImplBase {
        public abstract void upsertSession(UpsertSessionRequest req, StreamObserver<UpsertSessionResponse> resp);
        public abstract void removeSession(RemoveSessionRequest req, StreamObserver<RemoveSessionResponse> resp);
    }
}
