package com.chat.proto;

public final class RemoveSessionResponse {
    private final boolean success;
    public RemoveSessionResponse(boolean success) { this.success = success; }
    public boolean getSuccess() { return success; }
}
