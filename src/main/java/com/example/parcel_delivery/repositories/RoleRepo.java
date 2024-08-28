package com.example.parcel_delivery.repositories;

import java.util.Optional;
import java.util.Set;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.parcel_delivery.models.entities.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    @Query("SELECT r.name FROM Role r")
    Set<String> findAllRoleNames();

}
