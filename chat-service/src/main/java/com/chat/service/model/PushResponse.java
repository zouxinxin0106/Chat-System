package com.chat.service.model;

public final class PushResponse {
    private boolean success;
    private String error;

    public PushResponse() {
    }

    public PushResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
