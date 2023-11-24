package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Customer;

public interface CustomerService {

    Customer getCustomerById(Long customerId);

    Customer getCustomerByUserId(Long userId);

    Customer getCustomerByAuthenticatedUser();
    
}
