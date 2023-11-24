package com.example.parcel_delivery.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Driver;

public interface DriverRepo extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

}
