package com.example.parcel_delivery.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.models.enums.ParcelType;

public interface ParcelRepo extends JpaRepository<Parcel, Long> {

        @Modifying
        @Query("UPDATE Parcel p SET p.idempotencyKey = null, p.idempotencyKeyCreatedAt = null WHERE p.idempotencyKeyCreatedAt < :threshold")
        void nullifyOldIdempotencyKeys(LocalDateTime threshold);

        Optional<Parcel> findByIdempotencyKey(String idempotencyKey);

        List<Parcel> findBySenderId(Long id);

        List<Parcel> findByRecipientId(Long id);

        Optional<Parcel> findByIdAndSenderId(Long id, Long senderId);

        Optional<Parcel> findByIdAndRecipientId(Long id, Long recipientId);

        Page<Parcel> findByStatus(ParcelStatus status, Pageable pageable);

        Long countByDriver(Driver driver);

        @Query("SELECT p FROM Parcel p WHERE p.driver.id = :driverId AND p.status = :status")
        List<Parcel> findByDriverIdAndStatus(@Param("driverId") Long driverId, @Param("status") ParcelStatus status);

        @Query("SELECT p FROM Parcel p WHERE p.recipient.id = :recipientId AND p.status = :status")
        List<Parcel> findByRecipientIdAndStatus(@Param("recipientId") Long recipientId,
                        @Param("status") ParcelStatus status);

        @Query("SELECT p FROM Parcel p WHERE p.driver.id = :driverId")
        List<Parcel> findByDriverId(@Param("driverId") Long driverId);

        @Query("SELECT p FROM Parcel p WHERE p.driver.id = :driverId AND p.parcelType = :parcelType")
        List<Parcel> findByDriverIdAndParcelType(@Param("driverId") Long driverId,
                        @Param("parcelType") ParcelType parcelType);

        Optional<Parcel> findBySenderTransactionCode(Integer senderTransactionCode);

        Optional<Parcel> findByRecipientTransactionCode(Integer recipientTransactionCode);

        Long countByStatus(ParcelStatus status);

        @Query("SELECT p FROM Parcel p WHERE p.status IN :statuses")
        Page<Parcel> findByStatusIn(@Param("statuses") List<ParcelStatus> statuses, Pageable pageable);

        Optional<Parcel> findByIdAndDriverId(Long id, Long driverId);

        List<Parcel> findByStorageId(Long storageId);

        List<Parcel> findByStorageIdAndParcelType(Long storageId, ParcelType intraCity);

        @Query("SELECT p FROM Parcel p WHERE p.storage.city = :city AND p.status = :status")
        List<Parcel> findParcelsByCityAndStatus(@Param("city") String city,
                        @Param("status") ParcelStatus status,
                        Pageable pageable);

}
