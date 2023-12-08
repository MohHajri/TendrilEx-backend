package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Notification;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.enums.NotificationType;
import com.example.parcel_delivery.models.enums.ParcelStatus;

public interface NotificationService {

    Notification sendInAppNotification(Parcel savedParcel, NotificationType type, String message, User user);

    Notification sendEmailNotification(String recipientEmail, Integer transactionCode, String subject, String name, ParcelStatus status);
    
}
