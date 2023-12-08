package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.responses.NotificationResDTO;
import com.example.parcel_delivery.models.entities.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    
    @Mapping(source = "id", target = "notificationId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "type", target = "notificationType")
    @Mapping(source = "message", target = "notificationMessage")
    @Mapping(source = "createdAt", target = "notificationDateAndTime", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "read", target = "isRead")
    NotificationResDTO toNotificationResDTO(Notification notification);


    
}
