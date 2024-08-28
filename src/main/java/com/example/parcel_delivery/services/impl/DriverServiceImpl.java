package com.example.parcel_delivery.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.repositories.DriverRepo;
import com.example.parcel_delivery.services.DriverService;
import com.example.parcel_delivery.services.UserService;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepo driverRepository;

    @Autowired
    private UserService userService;

    @Override
    public Driver findAvailableDriverInCity(DriverType driverType, String city) {
        List<Driver> availableDrivers = driverRepository.findAvailableDriversByTypeAndCity(driverType, city);
        if (availableDrivers.isEmpty()) {
            throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No available driver found in " + city + " for " + driverType + " type");
        }
        return availableDrivers.get(0); // Assign the first available driver
    }

    @Override
    public List<Driver> findAllAvailableDriversInCity(DriverType driverType, String city) {
        return driverRepository.findAvailableDriversByTypeAndCity(driverType, city);
    }

    @Override
    public Long getAvailableDriverCount(DriverType driverType, String city) {
        return driverRepository.countAvailableDriversByTypeAndCity(driverType, city);
    }

    @Override
    public void updateDriverAvailability(Driver driver, Boolean isAvailable) {
        driver.setIsAvailable(isAvailable);
        driverRepository.save(driver); // Persist the updated availability status
    }

    
    /**
     * This method retrieves all active and available intra-city drivers in a specific city.
     * @param city The city to search for intra-city drivers.
     * @return A list of active and available intra-city drivers in the specified city.
     */
    public List<Driver> getActiveAvailableIntraCityDrivers(String city) {
        return findAllAvailableDriversInCity(DriverType.INTRA_CITY, city);
    }

    /**
     * This method retrieves all active and available inter-city drivers in a specific city.
     * @param city The city to search for inter-city drivers.
     * @return A list of active and available inter-city drivers in the specified city.
     */
    public List<Driver> getActiveAvailableInterCityDrivers(String city) {
        return findAllAvailableDriversInCity(DriverType.INTER_CITY, city);
    }


    public Driver getAuthenticatedDriver() {
        User authenticatedUser = userService.getAuthenticatedUser();
        Long userId = authenticatedUser.getId();

        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Driver not found"));
    }

}
