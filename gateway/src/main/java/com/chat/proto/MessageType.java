package com.chat.proto;

public enum MessageType {
    MESSAGE_TYPE_UNSPECIFIED(0),
    MESSAGE_TYPE_TEXT(1),
    MESSAGE_TYPE_IMAGE(2),
    MESSAGE_TYPE_VOICE(3),
    MESSAGE_TYPE_ATTENDANCE(4),
    MESSAGE_TYPE_READ_RECEIPT(5);

    private final int value;
    MessageType(int value) { this.value = value; }
    public int getValue() { return value; }
    public static MessageType forNumber(int n) {
        switch (n) {
            case 0: return MESSAGE_TYPE_UNSPECIFIED;
            case 1: return MESSAGE_TYPE_TEXT;
            case 2: return MESSAGE_TYPE_IMAGE;
            case 3: return MESSAGE_TYPE_VOICE;
            case 4: return MESSAGE_TYPE_ATTENDANCE;
            case 5: return MESSAGE_TYPE_READ_RECEIPT;
            default: return MESSAGE_TYPE_UNSPECIFIED;
        }
    }
}
