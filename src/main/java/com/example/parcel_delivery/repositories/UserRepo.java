package com.example.parcel_delivery.repositories;

import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.parcel_delivery.models.entities.User;

public interface UserRepo extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    @NonNull
    Optional<User> findByUsername(@Param("username") String username);

    @Override
    @NonNull
    Optional<User> findById(@NonNull Long id);

    @Override
    @NonNull
    List<User> findAll();

    Boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

}
