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

    /**
     * This method retreives a driver by it ID
     * 
     * @param driverId
     */
    @Override
    public Driver getDriverById(Long driverId) {
        return driverRepository.findById(
                driverId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Customer not found with id: " + driverId));
    }

    /**
     * This method is used by the batch system to know how many available drivers
     * there are based on the their city and type
     * 
     * @param driverType
     * @param city
     */
    @Override
    public Long getAvailableDriverCount(DriverType driverType, String city) {
        return driverRepository.countAvailableDriversByTypeAndCity(driverType, city);
    }

    /**
     * This method is used by the batch system to update driver availability
     * Normally, drivers right now can not update their availability status by
     * themselves
     * 
     * @param driver
     * @param isAvailable
     */
    @Override
    public void updateDriverAvailability(Driver driver, Boolean isAvailable) {
        driver.setIsAvailable(isAvailable);
        driverRepository.save(driver);
    }

    /**
     * This method retrieves all active and available intra-city drivers in a
     * specific city.
     * 
     * @param city The city to search for intra-city drivers.
     * @return A list of active and available intra-city drivers in the specified
     *         city.
     */
    public List<Driver> getActiveAvailableIntraCityDrivers(String city) {
        return driverRepository.findAvailableDriversByTypeAndCity(DriverType.INTRA_CITY, city);
    }

    /**
     * This method retrieves all active and available inter-city drivers in a
     * specific city.
     * 
     * @param city The city to search for inter-city drivers.
     * @return A list of active and available inter-city drivers in the specified
     *         city.
     */
    public List<Driver> getActiveAvailableInterCityDrivers(String city) {
        return driverRepository.findAvailableDriversByTypeAndCity(DriverType.INTER_CITY, city);
    }

    /**
     * This method is important. it gets the authenticated user associated with the
     * driver
     */
    public Driver getAuthenticatedDriver() {
        User authenticatedUser = userService.getAuthenticatedUser();
        Long userId = authenticatedUser.getId();

        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Driver not found"));
    }

    /**
     * Checks if a driver has parcels assigned.
     * It is used by the batch assignment system
     * 
     * @param driver The driver to check.
     * @return true if the driver has parcels assigned, false otherwise.
     */
    @Override
    public boolean hasParcelsAssigned(Driver driver) {
        Long parcelCount = driverRepository.countParcelsAssignedToDriver(driver.getId());
        return parcelCount > 0;
    }

    /**
     * Method used tp set a driver as unavailable if they have no parcels assigned.
     * It is an emergency operation called by the driver but as for now they have to
     * be have to have no parcles assigned to do so
     * 
     * @param driverId The ID of the driver to be marked as unavailable.
     * @throws TendrilExExceptionHandler if the driver is not found or has parcels
     *                                   assigned.
     */
    @Override
    public Driver markDriverAsUnavailable(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Driver not found"));

        // Check if the driver has parcels assigned
        if (hasParcelsAssigned(driver)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Driver cannot be marked as unavailable because they have parcels assigned");
        }

        driver.setIsAvailable(false);
        driverRepository.save(driver);
        return driver;
    }

}
