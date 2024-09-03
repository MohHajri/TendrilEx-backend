package com.example.parcel_delivery.repositories;

import org.springframework.lang.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.parcel_delivery.models.entities.Customer;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    @NonNull
    Optional<Customer> findByUserId(@NonNull Long userId);

    @NonNull
    Optional<Customer> findById(@NonNull Long customerId);

    @NonNull
    Optional<Customer> findByUserPhoneNumber(@NonNull String phoneNumber);

    @Query("SELECT c FROM Customer c JOIN c.user u JOIN u.roles r WHERE u.city = :city AND r.name = 'ROLE_USER'")
    @NonNull
    Page<Customer> findByUserCityAndRole(Pageable pageable, @NonNull String city);
}
