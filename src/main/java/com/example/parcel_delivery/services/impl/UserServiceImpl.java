package com.example.parcel_delivery.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.repositories.UserRepo;
import com.example.parcel_delivery.services.UserService;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepo userRepository;

    @Override
    public User getUserbyId(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "User not found with id: " + id));
       }

    @Override
    public User getUserbyUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "User not found with username: " + username));
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new TendrilExExceptionHandler(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String username = authentication.getName();
        return getUserbyUsername(username);         
    }
    
}
