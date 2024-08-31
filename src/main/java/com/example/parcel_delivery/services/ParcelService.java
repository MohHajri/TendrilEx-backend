package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.ParcelStatus;

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

    Long countParcelsByStatus(ParcelStatus status);

    List<Parcel> getUnassignedInterParcels(int page, int size);

    List<Parcel> getUnassignedIntraParcels(int page, int size);

    List<Parcel> getParcelsAssignedToIntraCityDriver(Long driverId);

    List<Parcel> getParcelsAssignedToInterCityDriver(Long driverId);

    List<Parcel> getParcelsInStorageAssignedToInterDriver(Long driverId);

    List<Parcel> getParcelsInStorageAssignedToIntraDriver(Long driverId);

    Parcel dropOffParcelInCabinet(Long parcelId, Integer transactionCode);

}
