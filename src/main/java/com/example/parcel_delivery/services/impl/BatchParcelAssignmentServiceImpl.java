
// package com.example.parcel_delivery.services.impl;

// import java.util.List;
// import java.util.ArrayList;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
// import com.example.parcel_delivery.models.entities.Driver;
// import com.example.parcel_delivery.models.entities.Parcel;
// import com.example.parcel_delivery.models.enums.DriverType;
// import com.example.parcel_delivery.models.enums.ParcelStatus;
// import com.example.parcel_delivery.models.enums.ParcelType;
// import com.example.parcel_delivery.services.BatchParcelAssignmentService;
// import com.example.parcel_delivery.services.DriverService;
// import com.example.parcel_delivery.services.ParcelService;
// import com.example.parcel_delivery.services.StorageService;

// @Service
// public class BatchParcelAssignmentServiceImpl implements BatchParcelAssignmentService {

//     @Autowired
//     private DriverService driverService;

//     @Autowired
//     private ParcelService parcelService;

//     @Autowired
//     private StorageService storageService;

//     // Define the minimum number of parcels each driver should handle per type
//     private static final int INTRA_CITY_PARCELS_PER_DRIVER = 4; // Intra-city parcels per driver
//     private static final int INTER_CITY_PARCELS_PER_DRIVER = 5; // Inter-city parcels per driver

//     @Transactional
//     // @Scheduled(cron = "0 0 1 * * MON-FRI")
//     public void batchAssignParcels() {
//         int page = 0;
//         List<Parcel> parcels;

//         do {
//             System.out.println("Beginning of batch");

//             // Fetch unassigned parcels that need to be assigned to drivers
//             parcels = parcelService.findParcelsForDriverAssignment(page, Math.max(INTRA_CITY_PARCELS_PER_DRIVER, INTER_CITY_PARCELS_PER_DRIVER));

//             // If no parcels are available, exit the batch process
//             if (parcels.isEmpty()) {
//                 System.out.println("No parcels available for assignment.");
//                 break;
//             }

//             // Group parcels by city to minimize database queries
//             Map<String, List<Parcel>> parcelsByCity = parcels.stream()
//                     .collect(Collectors.groupingBy(parcel -> parcel.getSender().getUser().getCity()));

//             // Process each city separately
//             for (Map.Entry<String, List<Parcel>> entry : parcelsByCity.entrySet()) {
//                 String city = entry.getKey();
//                 List<Parcel> cityParcels = entry.getValue();

//                 try {
//                     // Assign parcels for the specific city
//                     processCityParcels(cityParcels, city);
//                 } catch (Exception e) {
//                     System.err.println("Error during parcel assignment for city " + city + ": " + e.getMessage());
//                     throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR, "Batch assignment failed for city " + city + ": " + e.getMessage());
//                 }
//             }

//             page++;
//         } while (!parcels.isEmpty());
//     }

//     /**
//      * Process parcels for a specific city by dividing them into intra-city and inter-city parcels,
//      * and then assigning them to available drivers.
//      *
//      * @param parcels The list of parcels in the city.
//      * @param city    The name of the city.
//      */
//     private void processCityParcels(List<Parcel> parcels, String city) {
//         // Separate parcels into intra-city and inter-city
//         List<Parcel> intraCityParcels = new ArrayList<>();
//         List<Parcel> interCityParcels = new ArrayList<>();

//         for (Parcel parcel : parcels) {
//             if (parcel.getParcelType() == ParcelType.INTRA_CITY) {
//                 intraCityParcels.add(parcel);
//             } else if (parcel.getParcelType() == ParcelType.INTER_CITY) {
//                 interCityParcels.add(parcel);
//             }
//         }

//         // Assign intra-city parcels
//         assignIntraCityParcels(intraCityParcels, city);

//         // Assign inter-city parcels, considering return parcels from storage
//         assignInterCityParcels(interCityParcels, city);
//     }

//     /**
//      * Assigns intra-city parcels to available drivers in a specific city.
//      * Ensures that each driver receives the minimum required number of parcels.
//      * Parcels that do not meet the minimum requirement are not assigned.
//      *
//      * @param parcels The list of intra-city parcels to assign.
//      * @param city    The city where the drivers operate.
//      */
//     private void assignIntraCityParcels(List<Parcel> parcels, String city) {
//         if (parcels.isEmpty() || parcels.size() < INTRA_CITY_PARCELS_PER_DRIVER) {
//             return; // Do not assign parcels if they don't meet the minimum requirement
//         }

//         List<Driver> availableDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTRA_CITY, city);

//         for (Driver driver : availableDrivers) {
//             if (parcels.size() < INTRA_CITY_PARCELS_PER_DRIVER) {
//                 break; // Do not assign if remaining parcels are less than the minimum required
//             }

//             for (int i = 0; i < INTRA_CITY_PARCELS_PER_DRIVER && !parcels.isEmpty(); i++) {
//                 Parcel parcel = parcels.remove(0);
//                 assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
//             }

//             // Mark driver as unavailable if they have reached their parcel limit
//             if (parcelService.countParcelsByDriver(driver) >= INTRA_CITY_PARCELS_PER_DRIVER) {
//                 driverService.updateDriverAvailability(driver, false);
//             }
//         }
//     }

//     /**
//      * Assigns inter-city parcels to drivers and ensures that each driver also carries return parcels when available.
//      * Parcels that do not meet the minimum outgoing requirement are not assigned.
//      *
//      * @param outgoingParcels The list of outgoing parcels to be delivered to other cities.
//      * @param city            The city from which the parcels are being sent.
//      */
//     private void assignInterCityParcels(List<Parcel> outgoingParcels, String city) {
//         if (outgoingParcels.isEmpty() || outgoingParcels.size() < INTER_CITY_PARCELS_PER_DRIVER) {
//             return; // Do not assign outgoing parcels if they don't meet the minimum requirement
//         }

//         List<Driver> availableInterCityDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTER_CITY, city);

//         for (Driver driver : availableInterCityDrivers) {
//             // Fetch return parcels from the destination storage
//             List<Parcel> returnParcels = storageService.getParcelsForReturnTrip(driver.getUser().getCity(), PageRequest.of(0, INTER_CITY_PARCELS_PER_DRIVER));

//             if (outgoingParcels.size() < INTER_CITY_PARCELS_PER_DRIVER) {
//                 break; // Do not assign if remaining outgoing parcels are less than the minimum required
//             }

//             // Assign outgoing parcels
//             for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER && !outgoingParcels.isEmpty(); i++) {
//                 Parcel parcel = outgoingParcels.remove(0);
//                 assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
//             }

//             // Assign return parcels if they meet the minimum requirement, otherwise leave unassigned
//             if (returnParcels.size() >= INTER_CITY_PARCELS_PER_DRIVER) {
//                 for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER && !returnParcels.isEmpty(); i++) {
//                     Parcel parcel = returnParcels.remove(0);
//                     assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
//                 }
//             }

//             // Mark driver as unavailable if they have reached their parcel limit
//             if (parcelService.countParcelsByDriver(driver) >= 2 * INTER_CITY_PARCELS_PER_DRIVER) {
//                 driverService.updateDriverAvailability(driver, false);
//             }
//         }
//     }

//     /**
//      * Assigns a parcel to the specified driver and updates its status.
//      */
//     private void assignParcelToDriver(Parcel parcel, Driver driver, ParcelStatus status) {
//         parcel.setDriver(driver);
//         parcel.setStatus(status);
//         parcelService.save(parcel);
//     }
// }


package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
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

        List<Parcel> parcels;

        do {
            // Fetch unassigned parcels that need to be assigned to drivers
            parcels = parcelService.findParcelsForDriverAssignment(page, Math.max(INTRA_CITY_PARCELS_PER_DRIVER, INTER_CITY_PARCELS_PER_DRIVER));

            // If no parcels are available, exit the batch process
            if (parcels.isEmpty()) {

                break;
            }

            // Group parcels by city to minimize database queries
            Map<String, List<Parcel>> parcelsByCity = parcels.stream()
                    .collect(Collectors.groupingBy(parcel -> parcel.getSender().getUser().getCity()));

            // Process each city separately
            for (Map.Entry<String, List<Parcel>> entry : parcelsByCity.entrySet()) {

                String city = entry.getKey();

                List<Parcel> cityParcels = entry.getValue();

                try {
                    // Assign parcels for the specific city

                    processCityParcels(cityParcels, city);

                } catch (Exception e) {

                    throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR, "Batch assignment failed for city " + city + ": " + e.getMessage());
                }
            }

            page++;
        } while (!parcels.isEmpty());
    }

    /**
     * Process parcels for a specific city by dividing them into intra-city and inter-city parcels,
     * and then assigning them to available drivers.
     *
     * @param parcels The list of parcels in the city.
     * @param city    The name of the city.
     */
    private void processCityParcels(List<Parcel> parcels, String city) {
        // Separate parcels into intra-city and inter-city

        List<Parcel> intraCityParcels = new ArrayList<>();

        Map<String, List<Parcel>> interCityParcelsByDestination = parcels.stream()

                .filter(parcel -> parcel.getParcelType() == ParcelType.INTER_CITY)

                .collect(Collectors.groupingBy(parcel -> {

                    if (parcel.getRecipient() != null) {

                        return parcel.getRecipient().getUser().getCity();

                    } else {

                        return parcel.getUnregisteredRecipientCity();

                    }

                }));

        for (Parcel parcel : parcels) {

            if (parcel.getParcelType() == ParcelType.INTRA_CITY) {

                intraCityParcels.add(parcel);

            }
        }

        // Assign intra-city parcels
        assignIntraCityParcels(intraCityParcels, city);

        // Assign inter-city parcels, considering return parcels from storage
        assignInterCityParcels(interCityParcelsByDestination, city);
    }

    /**
     * Assigns intra-city parcels to available drivers in a specific city.
     * Ensures that each driver receives the minimum required number of parcels.
     * Parcels that do not meet the minimum requirement are not assigned.
     *
     * @param parcels The list of intra-city parcels to assign.
     * @param city    The city where the drivers operate.
     */
    private void assignIntraCityParcels(List<Parcel> parcels, String city) {

        if (parcels.isEmpty() || parcels.size() < INTRA_CITY_PARCELS_PER_DRIVER) {

            return; // Do not assign parcels if they don't meet the minimum requirement
        }

        List<Driver> availableDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTRA_CITY, city);

        for (Driver driver : availableDrivers) {

            if (parcels.size() < INTRA_CITY_PARCELS_PER_DRIVER) {

                break; // Do not assign if remaining parcels are less than the minimum required
            }

            for (int i = 0; i < INTRA_CITY_PARCELS_PER_DRIVER && !parcels.isEmpty(); i++) {

                Parcel parcel = parcels.remove(0);

                assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);

            }

            // Mark driver as unavailable if they have reached their parcel limit
            if (parcelService.countParcelsByDriver(driver) >= INTRA_CITY_PARCELS_PER_DRIVER) {

                driverService.updateDriverAvailability(driver, false);

            }
        }
    }

    /**
     * Assigns inter-city parcels to drivers and ensures that each driver also carries return parcels when available.
     * Parcels that do not meet the minimum outgoing requirement are not assigned.
     *
     * @param interCityParcelsByDestination The map of outgoing parcels grouped by destination city.
     * @param city                          The city from which the parcels are being sent.
     */
    private void assignInterCityParcels(Map<String, List<Parcel>> interCityParcelsByDestination, String city) {

        List<Driver> availableInterCityDrivers = driverService.findAllAvailableDriversInCity(DriverType.INTER_CITY, city);

        Random random = new Random();

        for (Driver driver : availableInterCityDrivers) {
            
            // Randomly select a destination city for this driver
            List<String> destinationCities = new ArrayList<>(interCityParcelsByDestination.keySet());

            if (destinationCities.isEmpty()) break;

            String destinationCity = destinationCities.get(random.nextInt(destinationCities.size()));

            List<Parcel> outgoingParcels = interCityParcelsByDestination.get(destinationCity);

            if (outgoingParcels.size() < INTER_CITY_PARCELS_PER_DRIVER) {

                continue; // Skip if there are not enough parcels to meet the minimum requirement
            }

            // Fetch return parcels from the destination storage
            List<Parcel> returnParcels = storageService.getParcelsForReturnTrip(destinationCity, PageRequest.of(0, INTER_CITY_PARCELS_PER_DRIVER));

            // Assign outgoing parcels
            for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER && !outgoingParcels.isEmpty(); i++) {

                Parcel parcel = outgoingParcels.remove(0);

                assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
            }

            // Assign return parcels if they meet the minimum requirement, otherwise leave unassigned
            if (returnParcels.size() >= INTER_CITY_PARCELS_PER_DRIVER) {

                for (int i = 0; i < INTER_CITY_PARCELS_PER_DRIVER && !returnParcels.isEmpty(); i++) {

                    Parcel parcel = returnParcels.remove(0);

                    assignParcelToDriver(parcel, driver, ParcelStatus.AWAITING_PICKUP);
                }
            }

            // Mark driver as unavailable if they have reached their parcel limit
            if (parcelService.countParcelsByDriver(driver) >= 2 * INTER_CITY_PARCELS_PER_DRIVER) {

                driverService.updateDriverAvailability(driver, false);

            }

        }
    }

    /**
     * Assigns a parcel to the specified driver and updates its status.
     */
    private void assignParcelToDriver(Parcel parcel, Driver driver, ParcelStatus status) {

        parcel.setDriver(driver);

        parcel.setStatus(status);

        parcelService.save(parcel);

    }
}
