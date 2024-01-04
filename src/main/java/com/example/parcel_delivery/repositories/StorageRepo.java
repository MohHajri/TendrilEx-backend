package com.example.parcel_delivery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Storage;
import java.util.Optional;

public interface StorageRepo extends JpaRepository<Storage, Long> {

    Optional<Storage> findByCity(String city);
    
}
