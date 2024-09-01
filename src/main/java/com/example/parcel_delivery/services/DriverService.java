package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.enums.DriverType;

public interface DriverService {

    Driver getDriverById(Long driverId);

    void updateDriverAvailability(Driver driver, Boolean isAvailable);

    List<Driver> getActiveAvailableIntraCityDrivers(String city);

    List<Driver> getActiveAvailableInterCityDrivers(String city);

    Long getAvailableDriverCount(DriverType driverType, String city);

    Driver getAuthenticatedDriver();

    boolean hasParcelsAssigned(Driver driver);

    Driver markDriverAsUnavailable(Long driverId);

}
