package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;

import com.example.parcel_delivery.models.dtos.responses.CustomerResDTO;
import com.example.parcel_delivery.models.entities.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResDTO toCustomerResDTO(Customer customer);
    
}
