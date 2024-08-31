package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.models.enums.ParcelType;
import com.example.parcel_delivery.services.BatchParcelAssignmentService;
import com.example.parcel_delivery.services.DriverService;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.services.StorageService;

@Service
public class BatchParcelAssignmentServiceImpl implements BatchParcelAssignmentService {

    @Autowired
    private DriverService driverService;

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private StorageService storageService;

    // Define the minimum number of parcels each driver should handle per type
    private static final int INTRA_CITY_PARCELS_PER_DRIVER = 4; // Intra-city parcels per driver
    private static final int INTER_CITY_PARCELS_PER_DRIVER = 5; // Inter-city parcels per driver


    @Transactional
    // @Scheduled(cron = "0 0 1 * * MON-FRI")
    public void batchAssignParcels() {

        int page = 0;

        int pageSize = 20; // Adjust based on system capability

        List<Parcel> parcels;

        do {
            // Fetch unassigned parcels for the current page( either intra or inter)
            parcels = parcelService.findParcelsForDriverAssignment(page, pageSize);

            if (parcels.isEmpty()) {

                break; // Exit if no parcels are available
            }

            // Group parcels by sender city to optimize driver assignment
            Map<String, List<Parcel>> parcelsByCity = parcels.stream()

                    .collect(Collectors.groupingBy(parcel -> parcel.getSender().getUser().getCity()));

            // Process parcels for each city
            for (Map.Entry<String, List<Parcel>> entry : parcelsByCity.entrySet()) {

                String city = entry.getKey();

                List<Parcel> cityParcels = entry.getValue();

                try {

                    processCityParcels(cityParcels, city);

                } catch (Exception e) {

                    throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Batch assignment failed for city " + city + ": " + e.getMessage());

                }
            }

            page++;

        } while (!parcels.isEmpty());
    }

    /**
     * Process parcels for a specific city by dividing them into intra-city and
     * inter-city parcels,
     * and then assigning them to available drivers based on predefined scenarios.
     *
     * @param parcels The list of parcels in the city.
     * @param city    The name of the city.
     */
    private void processCityParcels(List<Parcel> parcels, String city) {
        // Separate parcels into intra-city and inter-city
        //intra city parcels
        List<Parcel> intraCityParcels = parcels.stream()

                .filter(parcel -> parcel.getParcelType() == ParcelType.INTRA_CITY)

                .collect(Collectors.toList());

        // inter city parcels are grouped by destionation
        Map<String, List<Parcel>> interCityParcelsByDestination = parcels.stream()

                .filter(parcel -> parcel.getParcelType() == ParcelType.INTER_CITY)

                .collect(Collectors

                        .groupingBy(parcel -> parcel.getRecipient() != null ? parcel.getRecipient().getUser().getCity()

                                : parcel.getUnregisteredRecipientCity()));

        // Assign intra-city parcels
        assignIntraCityParcels(intraCityParcels, city);

        // assign inter-city parcels
        assignInterCityParcels(interCityParcelsByDestination, city);
    }

    /**
     * Assigns intra-city parcels to available drivers in a specific city.
     * Handles high volume, low volume, and end-of-batch scenarios.
     *
     * @param parcels The list of intra-city parcels to assign.
     * @param city    The city where the drivers operate.
     */
    private void assignIntraCityParcels(List<Parcel> parcels, String city) {

        if (parcels.isEmpty()) return; // No parcels to assign

        List<Driver> availableDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTRA_CITY, city);

        /** Scenario 1: High Volume Assignment
         * Purpose: This scenario exists to ensure that drivers are fully utilized by assigning them a specific threshold of parcels (INTRA_CITY_PARCELS_PER_DRIVER). 
         * The idea is to ensuring that each driver has a sufficient workload to justify their assignment for the day (the batch works daily at 1:00 am)
         * 
         */
        if (parcels.size() >= INTRA_CITY_PARCELS_PER_DRIVER) {

            for (Driver driver : availableDrivers) {

                if (parcels.isEmpty()) break;

                if (!driverService.hasParcelsAssigned(driver)) {

                    for (int i = 0; i < INTRA_CITY_PARCELS_PER_DRIVER && !parcels.isEmpty(); i++) {

                        Parcel parcel = parcels.remove(0);

                        // assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
                        assignParcelToDriver(parcel, driver, ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER);

                    }

                    if (parcelService.countParcelsByDriver(driver) >= INTRA_CITY_PARCELS_PER_DRIVER) {

                        driverService.updateDriverAvailability(driver, false);

                    }
                }
            }
        }

        /**Scenario 2: Low Volume Assignment
         * Purpose: This scenario is to ensure that even on low-volume days, parcels are still delivered rather than waiting until the next day, which could cause delays.
         * It's a fallback. it happens when the total number of parcels available on a given day is less than the threshold.
         * 
         */
        if (!parcels.isEmpty() && parcels.size() < INTRA_CITY_PARCELS_PER_DRIVER) {

            assignRemainingParcels(parcels, availableDrivers);

        }

        /**
         * Scenario 3: End-of-Batch Assignment
         * Purpose: To handle leftover parcels after processing the majority of parcels.
         */
        if (!parcels.isEmpty()) {

            assignRemainingParcels(parcels, availableDrivers);
        }
    }

    /**
     * Assigns inter-city parcels to drivers and ensures that each driver also
     * carries return parcels when available.
     * Handles high volume, low volume, and end-of-batch scenarios.
     *
     * @param interCityParcelsByDestination The map of outgoing parcels grouped by
     *                                      destination city.
     * @param city                          The city from which the parcels are
     *                                      being sent.
     */
    private void assignInterCityParcels(Map<String, List<Parcel>> interCityParcelsByDestination, String city) {

        List<Driver> availableInterCityDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTER_CITY, city);

        /**
         * Scenario 1: High Volume Assignment
         * As said above, purpose is to ensure drivers are fully utilized by assigning them a specific threshold of parcels.
         */
        for (Driver driver : availableInterCityDrivers) {

            if (interCityParcelsByDestination.isEmpty()) break;

            for (String destinationCity : interCityParcelsByDestination.keySet()) {

                List<Parcel> outgoingParcels = interCityParcelsByDestination.get(destinationCity);

                if (outgoingParcels == null || outgoingParcels.isEmpty()) continue;

                assignParcelsToDriver(outgoingParcels, driver, INTER_CITY_PARCELS_PER_DRIVER);

                // Also handle return parcels
                List<Parcel> returnParcels = storageService.getParcelsForReturnTrip(destinationCity,

                        PageRequest.of(0, INTER_CITY_PARCELS_PER_DRIVER));

                assignParcelsToDriver(returnParcels, driver, INTER_CITY_PARCELS_PER_DRIVER);

                if (parcelService.countParcelsByDriver(driver) >= 2 * INTER_CITY_PARCELS_PER_DRIVER) {

                    driverService.updateDriverAvailability(driver, false); // Mark driver as unavailable

                    break; // Move to the next driver
                }
            }
        }

        // Scenario 2 and 3: Assign any remaining parcels
        assignRemainingParcels(

                interCityParcelsByDestination.values()
                .stream()

                .flatMap(List::stream)

                .collect(Collectors.toList()

                ),

                availableInterCityDrivers);
    }

    /**
     * Assigns remaining parcels to the first available driver.
     *
     * @param parcels The list of parcels to assign.
     * @param drivers The list of available drivers.
     */
    private void assignRemainingParcels(List<Parcel> parcels, List<Driver> drivers) {

        for (Driver driver : drivers) {

            if (parcels.isEmpty())

                break;

            if (!driverService.hasParcelsAssigned(driver)) {

                assignParcelsToDriver(parcels, driver, parcels.size()); // Assign all remaining parcels

                driverService.updateDriverAvailability(driver, false); // Mark driver as unavailable
            }
        }
    }

    /**
     * Assigns parcels to a specific driver, respecting the maximum parcel limit per
     * driver.
     *
     * @param parcels    The list of parcels to assign.
     * @param driver     The driver to whom the parcels will be assigned.
     * @param maxParcels The maximum number of parcels to assign to this driver.
     */
    private void assignParcelsToDriver(List<Parcel> parcels, Driver driver, int maxParcels) {

        int parcelsToAssign = Math.min(parcels.size(), maxParcels);

        for (int i = 0; i < parcelsToAssign; i++) {

            Parcel parcel = parcels.remove(0);

            // assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
             // Update the status based on parcel type
            if (parcel.getParcelType() == ParcelType.INTRA_CITY) {

                assignParcelToDriver(parcel, driver, ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER);

            } else if (parcel.getParcelType() == ParcelType.INTER_CITY) {

                assignParcelToDriver(parcel, driver, ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

            }
            
        }

        if (parcelService.countParcelsByDriver(driver) >= maxParcels) {

            driverService.updateDriverAvailability(driver, false);

        }
    }

    /**
     * Assigns a parcel to the specified driver and updates its status.
     * This method ensures that the parcel is assigned and its status is updated in
     * the database.
     *
     * @param parcel The parcel to be assigned.
     * @param driver The driver to whom the parcel is being assigned.
     * @param status The status to set on the parcel after assignment.
     */
    private void assignParcelToDriver(Parcel parcel, Driver driver, ParcelStatus status) {

        parcel.setDriver(driver);

        parcel.setStatus(status);

        parcelService.save(parcel);

    }
}
