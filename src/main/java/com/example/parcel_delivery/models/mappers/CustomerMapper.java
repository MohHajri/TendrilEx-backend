package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.responses.CustomerResDTO;
import com.example.parcel_delivery.models.entities.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(source = "id", target = "customerId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.city", target = "city")
    CustomerResDTO toCustomerResDTO(Customer customer);

}
