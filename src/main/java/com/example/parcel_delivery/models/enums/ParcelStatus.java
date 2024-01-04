package com.example.parcel_delivery.models.enums;


public enum ParcelStatus {
    CREATED,
    AWAITING_DRIVER_ASSIGNMENT,
    ASSIGNED_TO_DRIVER,
    AWAITING_PICKUP, 
    IN_TRANSIT_TO_STORAGE,
    IN_TRANSIT_TO_RECIPIENT, 
    DELIVERED, 
}
