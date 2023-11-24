package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class NotificationResDTO {

    private String notificationId;
    private String userId;
    private String notificationType;
    private String notificationMessage;
    private String notificationDateAndTime; 
    private Boolean isRead;   
}
