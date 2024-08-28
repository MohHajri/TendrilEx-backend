package com.example.parcel_delivery.services;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.example.parcel_delivery.models.entities.Parcel;

import com.example.parcel_delivery.models.entities.Storage;

public interface StorageService {

    Storage findCityStorage(String storage);

    Storage storeParcel(Parcel parcel, String city);

    Storage findOrCreateStorageForCity(String city);

    List<Parcel> getParcelsForReturnTrip(String city, Pageable pageable);

    
}
