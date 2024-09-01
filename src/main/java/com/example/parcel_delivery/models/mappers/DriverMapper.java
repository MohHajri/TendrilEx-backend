package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.responses.DriverResDTO;
import com.example.parcel_delivery.models.entities.Driver;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(source = "id", target = "driverId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "isAvailable", target = "isAvailable")
    @Mapping(source = "driverType", target = "driverType")
    @Mapping(source = "user.city", target = "city")
    DriverResDTO toDriverResDTO(Driver driver);

}
