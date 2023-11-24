package com.example.parcel_delivery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Parcel;

public interface ParcelRepo extends JpaRepository<Parcel, Long> {
    
}
