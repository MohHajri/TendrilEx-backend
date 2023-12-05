package com.example.parcel_delivery.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.Parcel;

public interface ParcelRepo extends JpaRepository<Parcel, Long> {

    @Modifying
    @Query("UPDATE Parcel p SET p.idempotencyKey = null, p.idempotencyKeyCreatedAt = null WHERE p.idempotencyKeyCreatedAt < :threshold")
    void nullifyOldIdempotencyKeys(LocalDateTime threshold);

    Optional<Parcel> findByIdempotencyKey(String idempotencyKey);
    
}
