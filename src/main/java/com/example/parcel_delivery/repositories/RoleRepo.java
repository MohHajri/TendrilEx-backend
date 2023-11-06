package com.example.parcel_delivery.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parcel_delivery.models.entities.Role;


public interface RoleRepo extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
