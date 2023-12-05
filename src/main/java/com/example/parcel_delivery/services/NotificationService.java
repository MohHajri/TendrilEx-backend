package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Notification;
import com.example.parcel_delivery.models.entities.Parcel;

public interface NotificationService {

    Notification sendInAppNotification(Parcel savedParcel);

    Notification sendSmsNotification(String recipientPhoneNo, Integer transactionCode);
    
}
