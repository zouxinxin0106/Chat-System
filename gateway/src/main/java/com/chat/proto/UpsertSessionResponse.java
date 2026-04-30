package com.chat.proto;

public final class UpsertSessionResponse {
    private final boolean success;
    public UpsertSessionResponse(boolean success) { this.success = success; }
    public boolean getSuccess() { return success; }
}
