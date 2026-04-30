package com.chat.proto;

public enum DeliveryStatus {
    DELIVERY_STATUS_UNSPECIFIED(0),
    DELIVERY_STATUS_SENT(1),
    DELIVERY_STATUS_DELIVERED(2),
    DELIVERY_STATUS_READ(3);

    private final int value;
    DeliveryStatus(int value) { this.value = value; }
    public int getValue() { return value; }
    public static DeliveryStatus forNumber(int n) {
        switch (n) {
            case 0: return DELIVERY_STATUS_UNSPECIFIED;
            case 1: return DELIVERY_STATUS_SENT;
            case 2: return DELIVERY_STATUS_DELIVERED;
            case 3: return DELIVERY_STATUS_READ;
            default: return DELIVERY_STATUS_UNSPECIFIED;
        }
    }
}
