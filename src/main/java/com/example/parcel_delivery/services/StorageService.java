package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Storage;

public interface StorageService {

    Storage findCityStorage(String storage);
    
}
