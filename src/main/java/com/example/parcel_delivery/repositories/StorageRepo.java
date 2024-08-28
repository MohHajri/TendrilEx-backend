package com.example.parcel_delivery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StorageRepo extends JpaRepository<Storage, Long> {

    Optional<Storage> findByCity(String city);
    
    // Custom query to find parcels by status and storage city
    @Query("SELECT p FROM Parcel p WHERE p.storage.city = :city AND p.status = :status")
    List<Parcel> findParcelsByCityAndStatus(@Param("city") String city, 
                                            @Param("status") ParcelStatus status,
                                            Pageable pageable);
}
