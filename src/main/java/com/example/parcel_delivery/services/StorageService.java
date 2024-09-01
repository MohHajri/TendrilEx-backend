package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Parcel;

import com.example.parcel_delivery.models.entities.Storage;

public interface StorageService {

    Storage findCityStorage(String storage);

    Storage storeParcel(Parcel parcel, String city);

    Storage findOrCreateStorageForCity(String city);

}
