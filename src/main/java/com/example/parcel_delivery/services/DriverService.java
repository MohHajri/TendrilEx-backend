package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.enums.DriverType;

public interface DriverService {

Driver findAvailableDriverInCity(DriverType driverType, String city);

void updateDriverAvailability(Driver driver, Boolean isAvailable);

     List<Driver> findAllAvailableDriversInCity(DriverType driverType, String city);

    Long getAvailableDriverCount(DriverType driverType, String city);

    Driver getAuthenticatedDriver();



   
}
