package com.example.parcel_delivery.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.repositories.DriverRepo;
import com.example.parcel_delivery.services.DriverService;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepo driverRepository;

    @Override
    public Driver findAvailableDriverInCity(DriverType driverType, String city) {
    List<Driver> availableDrivers = driverRepository.findAvailableDriversByTypeAndCity(driverType, city);
    if (availableDrivers.isEmpty()) {
        throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No available driver found in " + city);
    }
    // just take the first one from the list
    return availableDrivers.get(0);
}

    @Override
    public void save(Driver driver) {
        driverRepository.save(driver);
      }

  
}
