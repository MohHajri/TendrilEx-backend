package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Parcel;

public interface ParcelService {

    Parcel sendNewParcel(ParcelReqDTO parcelReqDTO);

    Parcel getParcelById(Long id);

    Parcel getByParcelIdAndSenderId(Long id, Long senderId);

    Parcel getByParcelIdAndRecipientId(Long id, Long recipientId);

    List<Parcel> getSentParcelsByCustomerId(Long id);

    List<Parcel> getReceivedParcelsByCustomerId(Long id);

    Parcel driverPicksUp(Long parcelId);

    Parcel driverDelivers(Long parcelId);

    // List<Parcel> findParcelsForDriverAssignment();
    List<Parcel> findParcelsForDriverAssignment(int page, int size);
    
    void save(Parcel parcel);


    
    
}
