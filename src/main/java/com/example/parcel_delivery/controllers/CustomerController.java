package com.example.parcel_delivery.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.dtos.responses.CustomerResDTO;
import com.example.parcel_delivery.models.mappers.CustomerMapper;
import com.example.parcel_delivery.services.CustomerService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/id/{id}")
    public ResponseEntity<CustomerResDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity
                .ok(customerMapper
                        .toCustomerResDTO(customerService
                                .getCustomerById(id)));
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<CustomerResDTO> getCustomerByUserId(@PathVariable Long userId) {
        return ResponseEntity
                .ok(customerMapper
                        .toCustomerResDTO(customerService
                                .getCustomerByUserId(userId)));
    }

    @GetMapping("/authenticated")
    public ResponseEntity<CustomerResDTO> getCustomerByAuthenticatedUser() {
        return ResponseEntity
                .ok(customerMapper
                        .toCustomerResDTO(customerService
                                .getCustomerByAuthenticatedUser()));
    }

    @PutMapping("/sender/location")
    public ResponseEntity<CustomerResDTO> updateCustomerLocation(
            @RequestBody CustomerLocationReqDTO customerLocationReqDTO) {
        return ResponseEntity
                .ok(customerMapper
                        .toCustomerResDTO(customerService
                                .updateCustomerLocation(
                                        customerLocationReqDTO)));
    }

}
