package com.example.parcel_delivery.services;

// import java.util.List;

import com.example.parcel_delivery.models.entities.User;

public interface UserService {

    User getUserbyId(Long id);

    User getUserbyUsername(String username);

    User getAuthenticatedUser();    
}
