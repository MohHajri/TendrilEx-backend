package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Parcel;

public interface ParcelService {

    Parcel sendNewParcel(ParcelReqDTO parcelReqDTO);
    
}
