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
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.models.enums.ParcelType;
import com.example.parcel_delivery.services.BatchParcelAssignmentService;
import com.example.parcel_delivery.services.DriverService;
import com.example.parcel_delivery.services.ParcelService;

@Service
public class BatchParcelAssignmentServiceImpl implements BatchParcelAssignmentService {

    @Autowired
    private DriverService driverService;

    @Autowired
    private ParcelService parcelService;

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

                    assignParcelsToIntraDrivers(cityParcels, city);

                    assignParcelsToInterDrivers(cityParcels, city);

                } catch (Exception e) {

                    throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Batch assignment failed for city " + city + ": " + e.getMessage());

                }
            }

            page++;

        } while (!parcels.isEmpty());
    }

    private void assignParcelsToIntraDrivers(List<Parcel> parcels, String city) {

        // STEP1: i will access the parcels that are ready to be assigned to intra
        // drivers
        // which are inter parcels of status(AWAITING_DEPARTURE_STORAGE_PICKUP and
        // AWAITING_FINAL_DELIVERY) . And intra parcels of
        // status(AWAITING_INTRA_CITY_PICKUP)

        List<Parcel> parcelsForIntraDriver = parcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.AWAITING_DEPARTURE_STORAGE_PICKUP
                        && parcel.getParcelType() == ParcelType.INTER_CITY ||
                        parcel.getStatus() == ParcelStatus.AWAITING_FINAL_DELIVERY
                                && parcel.getParcelType() == ParcelType.INTER_CITY
                        ||
                        parcel.getStatus() == ParcelStatus.AWAITING_INTRA_CITY_PICKUP && parcel
                                .getParcelType() == ParcelType.INTRA_CITY)
                .collect(Collectors.toList());

        // STEP 2. Get the available intra drivers
        List<Driver> availableIntraDrivers = driverService.getActiveAvailableIntraCityDrivers(city);

        // STEP 3. DO THE SENARIOS

        /**
         * Scenario 1: High Volume Assignment
         * Purpose: This scenario exists to ensure that drivers are fully utilized by
         * assigning them a specific threshold of parcels
         * (INTRA_CITY_PARCELS_PER_DRIVER).
         * The idea is to ensuring that each driver has a sufficient workload to justify
         * their assignment for the day (the batch works daily at 1:00 am)
         * 
         */
        if (parcelsForIntraDriver.size() >= INTRA_CITY_PARCELS_PER_DRIVER) {

            for (Driver driver : availableIntraDrivers) {

                if (parcelsForIntraDriver.isEmpty())
                    break;

                if (!driverService.hasParcelsAssigned(driver)) {

                    for (int i = 0; i < INTRA_CITY_PARCELS_PER_DRIVER && !parcelsForIntraDriver.isEmpty(); i++) {

                        Parcel parcel = parcelsForIntraDriver.remove(0);

                        parcel.setDriver(driver);

                        parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER);

                        parcelService.save(parcel);

                    }

                    if (parcelService.countParcelsByDriver(driver) >= INTRA_CITY_PARCELS_PER_DRIVER) {

                        driverService.updateDriverAvailability(driver, false);

                    }
                }
            }
        }

        /**
         * Scenario 2: Low Volume Assignment
         * Purpose: This scenario is to ensure that even on low-volume days, parcels are
         * still delivered rather than waiting until the next day, which could cause
         * delays.
         * It's a fallback. it happens when the total number of parcels available on a
         * given day is less than the threshold.
         * 
         */
        /**
         * Scenario 3: End-of-Batch Assignment
         * Purpose: To handle leftover parcels after processing the majority of parcels.
         */
        if (!parcelsForIntraDriver.isEmpty() && parcelsForIntraDriver.size() < INTRA_CITY_PARCELS_PER_DRIVER) {

            // assgin these parcels that are under the threshold to ONE availabe intra
            // driver that is of course not assigned to any parcels

            // ALSO
            // this method will supposedly run after the above two , so in case of any
            // remaining parcel inside the list of parcels , assign them to ONE available
            // intra driver who of course has not been assigned any parcels before

            Driver firstDriver = availableIntraDrivers.get(0);

            // assgin all the parcels in the list to that driver
            if (!driverService.hasParcelsAssigned(firstDriver)) {

                for (int i = 0; i <= parcelsForIntraDriver.size(); i++) {
                    Parcel parcel = parcelsForIntraDriver.remove(0);

                    parcel.setDriver(firstDriver);

                    parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER);

                    parcelService.save(parcel);

                }

                driverService.updateDriverAvailability(firstDriver, false);
            }

        }

    }

    private void assignParcelsToInterDrivers(List<Parcel> parcels, String city) {

        // STEP 1. i will access the parcels that are ready to be assigned to inter
        // drivers
        // which are inter parcels of a status(AWAITING_INTER_CITY_PICKUP)

        List<Parcel> parcelsForInterDriver = parcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.AWAITING_INTER_CITY_PICKUP
                        && parcel.getParcelType() == ParcelType.INTER_CITY)
                .collect(Collectors.toList());

        // STEP 2. here i will group these parcels by their destination city for some
        // reason(
        // for a smoonther handling of retunr parcels)

        // inter city parcels are grouped by destionation
        Map<String, List<Parcel>> interCityParcelsByDestination = parcelsForInterDriver.stream()

                .filter(parcel -> parcel.getParcelType() == ParcelType.INTER_CITY)

                .collect(Collectors

                        .groupingBy(parcel -> parcel.getRecipient() != null ? parcel.getRecipient().getUser().getCity()

                                : parcel.getUnregisteredRecipientCity()));

        // STEP 3. Get the available inter drivers
        List<Driver> availableInterDrivers = driverService.getActiveAvailableInterCityDrivers(city);

        // run for every destination city
        for (String destinationCity : interCityParcelsByDestination.keySet()) {

            List<Parcel> outgoingParcels = interCityParcelsByDestination.get(destinationCity);

            if (outgoingParcels == null || outgoingParcels.isEmpty())
                continue;

            List<Parcel> returnParcels = parcelService.getParcelsForReturnTrip(destinationCity,

                    PageRequest.of(0, INTER_CITY_PARCELS_PER_DRIVER));

            for (Driver driver : availableInterDrivers) {

                /**
                 * Scenario 1: High Volume Assignment
                 * As said above, purpose is to ensure drivers are fully utilized by assigning
                 * them a specific threshold of parcels.
                 * 
                 */
                if (!interCityParcelsByDestination.isEmpty()
                        && interCityParcelsByDestination.size() < INTER_CITY_PARCELS_PER_DRIVER * 2) {

                    // assgin outgoing parcels
                    for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER; i++) {

                        Parcel parcel = outgoingParcels.remove(0);
                        parcel.setDriver(driver);

                        parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                        parcelService.save(parcel);

                    }

                    // assgin return ones
                    for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER; i++) {

                        Parcel parcel = returnParcels.remove(0);
                        parcel.setDriver(driver);

                        parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                        parcelService.save(parcel);

                    }

                    // check if driver is loaded
                    if (parcelService.countParcelsByDriver(driver) >= 2
                            * INTER_CITY_PARCELS_PER_DRIVER) {

                        driverService.updateDriverAvailability(driver, false); // Mark
                                                                               // driver as
                                                                               // unavailable

                        break; // Move to the next driver
                    }

                }

                // Scenario 2 and 3: Assign any remaining parcels
                // same manner we should be able to check if the number of parcels is less
                // than
                // the threshold of inter drivers which is in this case can be checked as a
                // total for the outgoing and return. so if there are less the total of the
                // two
                // thresholds so we should be able to go the following
                /*
                 * - assgin these parcels ( both the outgoing and return) to one driver.
                 * senario 2 means when the batch runs and finds out that there is few
                 * parcels
                 * at that day so it should assign these few parcels ( of wich the total is
                 * under the threshold x2)
                 * 
                 * so technically i guess that would work as a senario number 3 as well
                 */
                if (!parcelsForInterDriver.isEmpty() && parcelsForInterDriver
                        .size() < INTRA_CITY_PARCELS_PER_DRIVER * 2) {

                    Driver firstDriver = availableInterDrivers.get(0);

                    if (!driverService.hasParcelsAssigned(firstDriver)) {

                        // assgin all outgoing parcels to that driver
                        for (int i = 0; i < outgoingParcels.size(); i++) {

                            Parcel parcel = outgoingParcels.remove(0);
                            parcel.setDriver(firstDriver);

                            parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                            parcelService.save(parcel);

                        }

                        // assign all return parcels to that driver
                        for (int i = 0; i < returnParcels.size(); i++) {

                            Parcel parcel = returnParcels.remove(0);
                            parcel.setDriver(firstDriver);

                            parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                            parcelService.save(parcel);

                        }

                        // and just make driver unavailable at the end
                        driverService.updateDriverAvailability(driver, false);

                    }
                }

            }

        }

    }
}