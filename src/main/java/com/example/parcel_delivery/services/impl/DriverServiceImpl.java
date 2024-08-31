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

     /**
     * Checks if a driver has parcels assigned.
     * @param driver The driver to check.
     * @return true if the driver has parcels assigned, false otherwise.
     */
    @Override
    public boolean hasParcelsAssigned(Driver driver) {
        Long parcelCount = driverRepository.countParcelsAssignedToDriver(driver.getId());
        return parcelCount > 0;
    }


     /**
     * Marks a driver as unavailable if they have no parcels assigned.
     * @param driverId The ID of the driver to be marked as unavailable.
     * @throws TendrilExExceptionHandler if the driver is not found or has parcels assigned.
     */
    @Override
    public void markDriverAsUnavailable(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Driver not found"));

        // Check if the driver has parcels assigned
        if (hasParcelsAssigned(driver)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver cannot be marked as unavailable because they have parcels assigned");
        }

        driver.setIsAvailable(false);
        driverRepository.save(driver);
    }



}
