package com.example.parcel_delivery.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.repositories.ParcelRepo;
import com.example.parcel_delivery.services.CleanupService;

import jakarta.transaction.Transactional;

@Service
@EnableScheduling

public class CleanupServiceImpl implements CleanupService{

    @Autowired
    private ParcelRepo parcelRepository;

    @Override
    @Scheduled(fixedRate = 86400000) // runs once every 24 h
    @Transactional
    public void cleanupOldIdempotencyKeys() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); 
        parcelRepository.nullifyOldIdempotencyKeys(threshold);
   

       
    }
    
}
