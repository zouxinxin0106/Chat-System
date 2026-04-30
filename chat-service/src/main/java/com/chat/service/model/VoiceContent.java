package com.chat.service.model;

public final class VoiceContent {
    private final String url;
    private final int durationSeconds;

    public VoiceContent(String url, int durationSeconds) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        this.url = url;
        this.durationSeconds = durationSeconds;
    }

    public String url() { return url; }
    public int durationSeconds() { return durationSeconds; }
}
