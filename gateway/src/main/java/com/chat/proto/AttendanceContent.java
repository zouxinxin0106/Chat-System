package com.chat.proto;

public final class AttendanceContent {
    private final String eventName;
    private final boolean isCheckIn;
    public AttendanceContent(String eventName, boolean isCheckIn) { this.eventName = eventName; this.isCheckIn = isCheckIn; }
    public String getEventName() { return eventName; }
    public boolean getIsCheckIn() { return isCheckIn; }
}
