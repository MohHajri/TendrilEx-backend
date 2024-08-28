package com.example.parcel_delivery.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.enums.DriverType;

public interface DriverRepo extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    @Query("SELECT d FROM Driver d WHERE d.driverType = :driverType AND d.isAvailable = true AND d.user.city = :city")
    List<Driver> findAvailableDriversByTypeAndCity(@Param("driverType") DriverType driverType, @Param("city") String city);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.driverType = :driverType AND d.isAvailable = true AND d.user.city = :city")
    Long countAvailableDriversByTypeAndCity(@Param("driverType") DriverType driverType, @Param("city") String city);

}
