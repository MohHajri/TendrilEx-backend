package com.example.parcel_delivery.services;

import java.util.Optional;

import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.entities.Customer;

public interface CustomerService {

    Customer getCustomerById(Long customerId);

    Customer getCustomerByUserId(Long userId);

    Customer getCustomerByAuthenticatedUser();

    Optional<Customer> findCustomerByPhoneNumber(String recipientPhone);

    Customer updateCustomerLocation(CustomerLocationReqDTO customerLocationReqDTO);
    
}
