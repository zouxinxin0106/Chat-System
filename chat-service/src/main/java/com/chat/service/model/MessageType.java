package com.chat.service.model;

public enum MessageType {
    MESSAGE_TYPE_UNSPECIFIED(0),
    MESSAGE_TYPE_TEXT(1),
    MESSAGE_TYPE_IMAGE(2),
    MESSAGE_TYPE_VOICE(3),
    MESSAGE_TYPE_ATTENDANCE(4),
    MESSAGE_TYPE_READ_RECEIPT(5);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return MESSAGE_TYPE_UNSPECIFIED;
    }
}
