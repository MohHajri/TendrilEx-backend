package com.example.parcel_delivery.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.parcel_delivery.models.entities.Cabinet;

public interface CabinetRepo extends JpaRepository<Cabinet, Long> {

    @Query(value = "SELECT * FROM cabinets WHERE parcel_locker_id = :lockerId AND status = 'FREE' ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Cabinet> findRandomFreeCabinetByLockerId(@Param("lockerId") Long lockerId);
    

    
}
