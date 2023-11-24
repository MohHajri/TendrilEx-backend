package com.example.parcel_delivery.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.repositories.CustomerRepo;
import com.example.parcel_delivery.services.CustomerService;
import com.example.parcel_delivery.services.UserService;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired
    private UserService userService;

    @Override
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Customer not found with id: " + customerId));
                    }

    @Override
    public Customer getCustomerByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Customer not found with user id: " + userId));
                    }

    @Override
    public Customer getCustomerByAuthenticatedUser() {
        User authUser = userService.getAuthenticatedUser();
        Long authUserId = authUser.getId();
        return customerRepository.findByUserId(authUserId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Customer not found with user id: " + authUserId));
                    
                    }
    
}
