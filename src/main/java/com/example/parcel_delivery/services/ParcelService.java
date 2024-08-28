package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;

public interface ParcelService {

    Parcel sendNewParcel(ParcelReqDTO parcelReqDTO);

    Parcel getParcelById(Long id);

    Parcel getByParcelIdAndSenderId(Long id, Long senderId);

    Parcel getByParcelIdAndRecipientId(Long id, Long recipientId);

    List<Parcel> getSentParcelsByCustomerId(Long id);

    List<Parcel> getReceivedParcelsByCustomerId(Long id);

    List<Parcel> findParcelsForDriverAssignment(int page, int size);

    Long countParcelsByDriver(Driver driver);

    Parcel pickUpParcelFromLocker(Long parcelId, Integer transactionCode);

    Parcel deliverToDestinationStorage(Long parcelId);

    Parcel deliverToDepartureStorage(Long parcelId);

    Parcel deliverToRecipient(Long parcelId);

    void save(Parcel parcel);

    List<Parcel> getParcelsAssignedToIntraCityDriver(Long driverId);

    List<Parcel> getParcelsAssignedToInterCityDriver(Long driverId);

    Parcel dropOffParcelInCabinet(Long parcelId, Integer transactionCode);





    
    
}
