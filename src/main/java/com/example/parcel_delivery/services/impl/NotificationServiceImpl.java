package com.example.parcel_delivery.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.models.dtos.responses.NotificationResDTO;
import com.example.parcel_delivery.models.entities.Notification;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.enums.NotificationType;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.models.mappers.NotificationMapper;
import com.example.parcel_delivery.repositories.NotificationRepo;
import com.example.parcel_delivery.services.EmailService;
import com.example.parcel_delivery.services.NotificationService;


@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired 
    private NotificationRepo notificationRepo;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired 
    private NotificationMapper notificationMapper;

    @Override
    public Notification sendInAppNotification(Parcel savedParcel, NotificationType type, String message, User user ) {

        Notification notification = new Notification();
        notification.setParcel(savedParcel);
        notification.setType(type);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(notification);
        
        NotificationResDTO notificationDTO = notificationMapper.toNotificationResDTO(notification);
        simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications", notificationDTO);
    
        return notification;
        }

    @Override
    public Notification sendEmailNotification(String recipientEmail, Integer transactionCode, String subject, String name, ParcelStatus status) {
        try {
            // emailService.sendHtmlEmail(recipientEmail, subject, name, status, transactionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
