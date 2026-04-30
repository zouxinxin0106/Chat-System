package com.chat.service.handler;

import com.chat.service.model.DeliveryStatus;

public final class DeliveryResult {
    private final boolean success;
    private final DeliveryStatus deliveryStatus;
    private final String errorMessage;

    public DeliveryResult(boolean success, DeliveryStatus deliveryStatus, String errorMessage) {
        this.success = success;
        this.deliveryStatus = deliveryStatus;
        this.errorMessage = errorMessage;
    }

    public boolean success() { return success; }
    public DeliveryStatus deliveryStatus() { return deliveryStatus; }
    public String errorMessage() { return errorMessage; }

    public static DeliveryResult success(DeliveryStatus status) {
        return new DeliveryResult(true, status, null);
    }

    public static DeliveryResult failure(String errorMessage) {
        return new DeliveryResult(false, DeliveryStatus.DELIVERY_STATUS_UNSPECIFIED, errorMessage);
    }
}
