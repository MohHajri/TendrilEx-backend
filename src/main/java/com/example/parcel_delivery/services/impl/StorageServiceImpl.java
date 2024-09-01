package com.example.parcel_delivery.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.Storage;
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
                .orElseThrow(
                        () -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No storage found in city: " + city));
    }

    /**
     * Finds the storage for the given city. If it does not exist, a new storage is
     * created.
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

}
