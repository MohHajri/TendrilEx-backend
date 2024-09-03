package com.example.parcel_delivery.repositories;

import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.ParcelLocker;

public interface ParcelLockerRepo extends JpaRepository<ParcelLocker, Long> {

    @Query(value = """
            SELECT l.*, COUNT(c.id) as free_cabinets_count \
            FROM parcel_lockers l \
            JOIN cabinets c ON l.id = c.parcel_locker_id \
            WHERE c.status = 'FREE' \
            AND ST_DWithin(l.geo_location, :customerLocation, 20000) \
            GROUP BY l.id \
            ORDER BY ST_DistanceSphere(l.geo_location, :customerLocation) ASC\
            """, nativeQuery = true)
    @NonNull
    List<ParcelLocker> getFiveNearestAvailablelockers(@NonNull Point customerLocation);

    @Override
    @NonNull
    Optional<ParcelLocker> findById(@NonNull Long selectedLockerId);
}
