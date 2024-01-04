package com.example.parcel_delivery.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.services.BatchParcelAssignmentService;
import com.example.parcel_delivery.services.DriverService;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.services.StorageService;


@Service
public class BatchParcelAssignmentServiceImpl  implements BatchParcelAssignmentService {

    @Autowired
    private DriverService driverService;

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private StorageService storageService;

    private static final int MAX_PARCELS_PER_DRIVER = 1;



    @Scheduled(fixedRate = 10000) // runs once every 10 s
    // @Scheduled(cron = "0 0 1 * * MON-FRI") // runs once every day at 1 am
    public void batchAssignParcels(){

        int page = 0;
        final int size = MAX_PARCELS_PER_DRIVER;
        List<Parcel> parcels;

        do {
            parcels = parcelService.findParcelsForDriverAssignment(page, size);
            parcels.parallelStream().forEach(parcel -> {
                processParcelAssignment(parcel);
            });
            page++;
        } while (!parcels.isEmpty());

    }


    private void processParcelAssignment(Parcel parcel) {

        try {
            DriverType driverType = determineDriverType(parcel);
            if (driverType == DriverType.INTRA_CITY){
                assignDriverToParcel(parcel, DriverType.INTRA_CITY);
            } else if (driverType == DriverType.INTER_CITY){
                moveToStorage(parcel);
                assignDriverToParcel(parcel, DriverType.INTER_CITY);
            } 

        } catch (Exception e) {
            e.printStackTrace();
        }
   
    }

    private DriverType determineDriverType(Parcel parcel) {
        String senderCity = parcel.getSender().getUser().getCity();
        String recipientCity = parcel.getIsRecipientRegistered() 
            ? parcel.getRecipient().getUser().getCity() 
            : parcel.getUnregisteredRecipientCity();

        return senderCity.equals(recipientCity) ? DriverType.INTRA_CITY : DriverType.INTER_CITY;
       
    }

    private void assignDriverToParcel(Parcel parcel, DriverType type) {

        Driver availableDriver = findAvailableDriverInCity(type, parcel.getRecipient().getUser().getCity());

        if (availableDriver != null) {
            parcel.setDriver(availableDriver);
            parcel.setStatus(ParcelStatus.ASSIGNED_TO_DRIVER);
            parcelService.save(parcel);

            // update driver availability
            updateDriverAvailability(availableDriver, false); 
        }
       
    }

    private void moveToStorage(Parcel parcel) {
        Storage storage = storageService.findCityStorage(parcel.getRecipient().getUser().getCity());
        parcel.setStorage(storage);
        parcel.setStatus(ParcelStatus.IN_TRANSIT_TO_STORAGE);
        parcelService.save(parcel);
        
    }

    private Driver findAvailableDriverInCity(DriverType driverType, String city) {
         return driverService.findAvailableDriverInCity(driverType, city);    
    }

    private void updateDriverAvailability(Driver driver, Boolean isAvailable) {
        driver.setIsAvailable(isAvailable);
        driverService.save(driver);

    }




}
