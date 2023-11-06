package com.example.parcel_delivery.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.User;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    List<User> findAll();

    Boolean existsByUsername(String username);

}
