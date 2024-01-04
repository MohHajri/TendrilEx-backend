package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.enums.DriverType;

public interface DriverService {

Driver findAvailableDriverInCity(DriverType driverType, String city);

void save(Driver driver);

   
}
