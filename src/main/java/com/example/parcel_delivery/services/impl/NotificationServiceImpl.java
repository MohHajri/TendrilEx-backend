package com.example.parcel_delivery.services.impl;

import org.springframework.stereotype.Service;

import com.example.parcel_delivery.models.entities.Notification;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.services.NotificationService;


@Service
public class NotificationServiceImpl implements NotificationService{

    @Override
    public Notification sendInAppNotification(Parcel savedParcel) {
       return null;
        }

    @Override
    public Notification sendSmsNotification(String recipientPhoneNo, Integer transactionCode) {
        return null;
    }
    
}
