package com.example.parcel_delivery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Notification;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    
}
