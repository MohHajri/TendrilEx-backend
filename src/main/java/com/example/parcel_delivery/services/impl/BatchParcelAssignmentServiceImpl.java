package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    private static final int INTRA_CITY_PARCELS_PER_DRIVER = 4;
    private static final int INTER_CITY_PARCELS_PER_DRIVER = 5;

    @Transactional
    // @Scheduled(cron = "0 0 1 * * MON-FRI")
    public void batchAssignParcels() {

        // Fetch unassigned parcels
        List<Parcel> parcels = parcelService.findParcelsForDriverAssignment();

        if (parcels.isEmpty()) {

            return; // Exit if no parcels are available
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

    }

    private void assignParcelsToIntraDrivers(List<Parcel> parcels, String city) {

        /*
         * STEP1: access the parcels that are ready to be assigned to intra
         * drivers
         * which are inter parcels of status:
         * - AWAITING_DEPARTURE_STORAGE_PICKUP
         * - AWAITING_FINAL_DELIVERY)
         * 
         * And intra parcels of status:
         * - AWAITING_INTRA_CITY_PICKUP)
         */

        List<Parcel> parcelsForIntraDriver = parcels.stream()
                .filter(parcel -> parcel.getStatus() == ParcelStatus.AWAITING_DEPARTURE_STORAGE_PICKUP
                        && parcel.getParcelType() == ParcelType.INTER_CITY ||
                        parcel.getStatus() == ParcelStatus.AWAITING_FINAL_DELIVERY
                                && parcel.getParcelType() == ParcelType.INTER_CITY
                        ||
                        parcel.getStatus() == ParcelStatus.AWAITING_INTRA_CITY_PICKUP && parcel
                                .getParcelType() == ParcelType.INTRA_CITY)
                .collect(Collectors.toList());

        /*
         * STEP 2. Get the available intra drivers
         * 
         */
        List<Driver> availableIntraDrivers = driverService.getActiveAvailableIntraCityDrivers(city);

        /*
         * STEP 3. Assign.
         */

        /**
         * Scenario 1: High Volume Assignment
         * ****************
         * 
         * Purpose: This scenario exists to ensure that drivers are fully utilized by
         * assigning them a specific threshold of parcels
         * (INTRA_CITY_PARCELS_PER_DRIVER).
         * The idea is to ensuring that each driver has a sufficient workload to justify
         * their assignment for the day (the batch works daily at 1:00 am)
         * 
         */
        if (!parcelsForIntraDriver.isEmpty() && parcelsForIntraDriver.size() >= INTRA_CITY_PARCELS_PER_DRIVER) {

            for (Driver driver : availableIntraDrivers) {

                if (parcelsForIntraDriver.isEmpty())
                    break;

                int parcelsToAssign = Math.min(parcelsForIntraDriver.size(), INTRA_CITY_PARCELS_PER_DRIVER);

                for (int i = 0; i < parcelsToAssign; i++) {

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

        /**
         * Scenario 2: Low Volume Assignment
         *************
         * 
         * Purpose: This scenario is to ensure that even on low-volume days, parcels are
         * still delivered rather than waiting until the next day, which could cause
         * delays.
         * It's a fallback. it happens when the total number of parcels available on a
         * given day is less than the threshold.
         * these parcels will be assigned to the first available one driver
         * 
         * Scenario 3: End-of-Batch Assignment
         * ******************
         * 
         * Purpose: To handle leftover parcels after processing the majority of parcels.
         */
        if (!parcelsForIntraDriver.isEmpty() && parcelsForIntraDriver.size() < INTRA_CITY_PARCELS_PER_DRIVER) {

            Driver firstDriver = availableIntraDrivers.get(0);

            for (Parcel parcel : parcelsForIntraDriver) {

                parcel.setDriver(firstDriver);

                parcel.setStatus(ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER);

                parcelService.save(parcel);

            }

            driverService.updateDriverAvailability(firstDriver, false);
        }

    }

    private void assignParcelsToInterDrivers(List<Parcel> parcels, String city) {

        /*
         * STEP 1. i will access the parcels that are ready to be assigned to inter
         * drivers
         * which are inter parcels of a status(AWAITING_INTER_CITY_PICKUP)
         * STEP 2. group these parcels by their destination city for some
         * reason( for a smoonther handling of retunrn parcels)
         */

        Map<String, List<Parcel>> interCityParcelsByDestination = parcels.stream()

                .filter(parcel -> parcel.getStatus() == ParcelStatus.AWAITING_INTER_CITY_PICKUP

                        && parcel.getParcelType() == ParcelType.INTER_CITY)

                .collect(Collectors.groupingBy(parcel -> parcel.getRecipient() != null

                        ? parcel.getRecipient().getUser().getCity()

                        : parcel.getUnregisteredRecipientCity()));

        List<Driver> availableInterDrivers = driverService.getActiveAvailableInterCityDrivers(city);

        // run for every destination city
        for (String destinationCity : interCityParcelsByDestination.keySet()) {

            List<Parcel> outgoingParcels = interCityParcelsByDestination.get(destinationCity);

            if (outgoingParcels == null || outgoingParcels.isEmpty())
                continue;

            List<Parcel> returnParcels = parcelService.getParcelsForReturnTrip(destinationCity, city);

            if (returnParcels.isEmpty() && outgoingParcels.isEmpty()) {
                break; // No more parcels to process
            }

            /**
             * Scenario 1: High Volume Assignment
             * As said above, purpose is to ensure drivers are fully utilized by assigning
             * them a specific threshold of parcels.
             * 
             */

            if (outgoingParcels.size() + returnParcels.size() >= INTER_CITY_PARCELS_PER_DRIVER * 2) {

                for (Driver driver : availableInterDrivers) {

                    if (outgoingParcels.isEmpty() && returnParcels.isEmpty())

                        break;

                    int outgoingToAssign = Math.min(outgoingParcels.size(), INTER_CITY_PARCELS_PER_DRIVER);

                    int returnToAssign = Math.min(returnParcels.size(), INTER_CITY_PARCELS_PER_DRIVER);

                    assignParcelsToDriver(driver, outgoingParcels, outgoingToAssign,

                            ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                    assignParcelsToDriver(driver, returnParcels, returnToAssign,

                            ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                    // check if driver is loaded by now

                    if (parcelService.countParcelsByDriver(driver) >= 2 * INTER_CITY_PARCELS_PER_DRIVER) {

                        driverService.updateDriverAvailability(driver, false);

                        break;
                    }
                }

            } else {

                /*
                 * Scenario 2 & 3:
                 * if there are only few inter parcels ( fewer than the threshold of inter
                 * parcels multiplied by two), then we will assgin all of these few parcels into
                 * one driver
                 * this works or gets triggered in two cases:
                 * - when there are only few inter parcels at that city during that day -->
                 * Scenario 2
                 * - when, at the end of the first scenario, there are few parcels left then
                 * they are assgined here
                 */
                for (Driver driver : availableInterDrivers) {

                    if (outgoingParcels.isEmpty() && returnParcels.isEmpty())

                        break;

                    if (!driver.getIsAvailable())
                        continue;

                    int outgoingToAssign = Math.min(outgoingParcels.size(), INTER_CITY_PARCELS_PER_DRIVER);

                    int returnToAssign = Math.min(returnParcels.size(), INTER_CITY_PARCELS_PER_DRIVER);

                    assignParcelsToDriver(driver, outgoingParcels,
                            outgoingToAssign,

                            ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                    assignParcelsToDriver(driver, returnParcels,
                            returnToAssign,

                            ParcelStatus.ASSIGNED_TO_INTER_CITY_DRIVER);

                    driverService.updateDriverAvailability(driver, false);

                }
            }

        }

    }

    private void assignParcelsToDriver(Driver driver, List<Parcel> parcels, int count, ParcelStatus status) {

        for (int i = 0; i < count && !parcels.isEmpty(); i++) {

            Parcel parcel = parcels.remove(0);

            parcel.setDriver(driver);

            parcel.setStatus(status);

            parcelService.save(parcel);

        }
    }
}