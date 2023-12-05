package com.example.parcel_delivery.services;

import java.util.List;

import com.example.parcel_delivery.models.dtos.requests.ParcelLockerReqDTO;
import com.example.parcel_delivery.models.entities.ParcelLocker;

public interface ParcelLockerService {

    List<ParcelLocker> getFiveNearestAvailablelockers(ParcelLockerReqDTO senderLocation);

    ParcelLocker getParcelLockerById(Long selectedLockerId);
    
}
