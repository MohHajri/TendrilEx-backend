package com.example.parcel_delivery.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.repositories.StorageRepo;
import com.example.parcel_delivery.services.StorageService;
import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import org.springframework.http.HttpStatus;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private StorageRepo storageRepository;

    /**
     * Stores the given parcel in the storage associated with the specified city.
     * If the storage does not exist, it is created.
     *
     * @param parcel The parcel to be stored.
     * @param city   The city where the storage is located.
     * @return The storage where the parcel is stored.
     */
    @Override
    @Transactional
    public Storage storeParcel(Parcel parcel, String city) {
        Storage storage = findOrCreateStorageForCity(city);

        parcel.setStorage(storage);

        storage.getParcels().add(parcel);

        storageRepository.save(storage);

        return storage;
    }

    /**
     * Finds the storage in the specified city.
     *
     * @param city The city to search for storage.
     * @return The found storage.
     * @throws TendrilExExceptionHandler if no storage is found.
     */
    @Override
    public Storage findCityStorage(String city) {
        return storageRepository.findByCity(city)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No storage found in city: " + city));
    }

    /**
     * Finds the storage for the given city. If it does not exist, a new storage is created.
     *
     * @param city The city where the storage is located.
     * @return The found or newly created storage.
     */
    @Override
    public Storage findOrCreateStorageForCity(String city) { 
        return storageRepository.findByCity(city)
                .orElseGet(() -> createStorageForCity(city));
    }

    /**
     * Creates a new storage for the specified city.
     *
     * @param city The city where the new storage will be created.
     * @return The newly created storage.
     */
    private Storage createStorageForCity(String city) {
        Storage storage = new Storage();
        storage.setCity(city);

        return storageRepository.save(storage);
    }


    /**
     * Retrieves parcels that are currently in storage and are ready for a return trip.
     *
     * @param city The city where the storage is located.
     * @param pageable Pagination information for limiting the number of parcels returned.
     * @return A list of parcels ready for a return trip.
     */
    @Override
    public List<Parcel> getParcelsForReturnTrip(String city, Pageable pageable) {
        return storageRepository.findParcelsByCityAndStatus(city, ParcelStatus.DELIVERED_TO_DEPARTURE_STORAGE, pageable);
    }

}
