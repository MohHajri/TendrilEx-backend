package com.example.parcel_delivery.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.ParcelStatus;

public interface ParcelRepo extends JpaRepository<Parcel, Long> {

    @Modifying
    @Query("UPDATE Parcel p SET p.idempotencyKey = null, p.idempotencyKeyCreatedAt = null WHERE p.idempotencyKeyCreatedAt < :threshold")
    void nullifyOldIdempotencyKeys(LocalDateTime threshold);

    Optional<Parcel> findByIdempotencyKey(String idempotencyKey);

    List <Parcel> findBySenderId(Long id);

    List <Parcel> findByRecipientId(Long id);

    Optional<Parcel> findByIdAndSenderId(Long id, Long senderId);

    Optional<Parcel> findByIdAndRecipientId(Long id, Long recipientId);

    // List<Parcel> findByStatus(ParcelStatus status);

    Page<Parcel> findByStatus(ParcelStatus status, Pageable pageable);
}
