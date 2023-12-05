package com.example.parcel_delivery.repositories;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.ParcelLocker;

public interface ParcelLockerRepo extends JpaRepository<ParcelLocker, Long> {
    
    @Query(value = "SELECT l.*, COUNT(c.id) as free_cabinets_count " + 
                   "FROM parcel_lockers l " +
                   "JOIN cabinets c ON l.id = c.parcel_locker_id " +
                   "WHERE c.status = 'FREE' " +
                   "AND ST_DWithin(l.location_point, :senderLocation, 20000) " +
                   "GROUP BY l.id " +
                   "ORDER BY ST_DistanceSphere(l.location_point, :senderLocation) ASC", 
          nativeQuery = true)
    List<ParcelLocker> getFiveNearestAvailablelockers(Point senderLocation); 
    
    Optional<ParcelLocker> findById(Long selectedLockerId);
}
