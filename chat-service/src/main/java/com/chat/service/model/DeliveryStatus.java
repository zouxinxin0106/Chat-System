package com.chat.service.model;

public enum DeliveryStatus {
    DELIVERY_STATUS_UNSPECIFIED(0),
    DELIVERY_STATUS_SENT(1),
    DELIVERY_STATUS_DELIVERED(2),
    DELIVERY_STATUS_READ(3);

    private final int value;

    DeliveryStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeliveryStatus fromValue(int value) {
        for (DeliveryStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return DELIVERY_STATUS_UNSPECIFIED;
    }
}
