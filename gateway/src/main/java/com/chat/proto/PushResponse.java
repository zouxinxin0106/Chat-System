package com.chat.proto;

public final class PushResponse {
    private final boolean success;
    private final String error;
    public PushResponse(boolean success, String error) { this.success = success; this.error = error; }
    public boolean getSuccess() { return success; }
    public String getError() { return error; }

    public static Builder newBuilder() { return new Builder(); }
    public static final class Builder {
        private boolean success;
        private String error;
        public Builder setSuccess(boolean v) { success = v; return this; }
        public Builder setError(String v) { error = v; return this; }
        public PushResponse build() { return new PushResponse(success, error); }
    }
}
