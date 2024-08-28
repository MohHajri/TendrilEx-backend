package com.example.parcel_delivery.repositories;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.Customer;

import org.springframework.data.domain.Page;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findById(Long customerId);

    Optional<Customer> findByUserPhoneNumber(String phoneNumber);

    // Page<Customer> findByUserCity(String city, Pageable pageable);

    @Query("SELECT c FROM Customer c JOIN c.user u JOIN u.roles r WHERE u.city = :city AND r.name = 'ROLE_USER'")
    Page<Customer> findByUserCityAndRole(Pageable pageable, String city);


}
