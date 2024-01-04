package com.example.parcel_delivery.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findById(Long customerId);

    Optional<Customer> findByUserPhoneNumber(String phonenNumber);    
}