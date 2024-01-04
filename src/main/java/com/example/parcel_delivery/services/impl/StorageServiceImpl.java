package com.example.parcel_delivery.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.repositories.StorageRepo;
import com.example.parcel_delivery.services.StorageService;


@Service
public class StorageServiceImpl  implements StorageService {

    @Autowired
    private StorageRepo storageRepository;

    @Override
    public Storage findCityStorage(String city) {

        return storageRepository.findByCity(city)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No storage found"));
         }
    
}
