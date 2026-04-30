package com.chat.proto;

public final class VoiceContent {
    private final String url;
    private final int durationSeconds;
    public VoiceContent(String url, int durationSeconds) { this.url = url; this.durationSeconds = durationSeconds; }
    public String getUrl() { return url; }
    public int getDurationSeconds() { return durationSeconds; }
}
