package com.example.parcel_delivery.models.enums;

public enum NotificationType {
    PICKUP_READY, // for recipients
    PRCEL_IN_TRANSIT, // for users (recipients, senders)
    NEW_PARCEL, //for drivers
    PARCEL_PICKEDUP, // for users (senders and driver)
}