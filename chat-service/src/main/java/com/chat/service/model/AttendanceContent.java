package com.chat.service.model;

public final class AttendanceContent {
    private final String eventName;
    private final boolean isCheckIn;

    public AttendanceContent(String eventName, boolean isCheckIn) {
        this.eventName = eventName;
        this.isCheckIn = isCheckIn;
    }

    public String eventName() { return eventName; }
    public boolean isCheckIn() { return isCheckIn; }
}
