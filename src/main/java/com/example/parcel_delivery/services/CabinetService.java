package com.example.parcel_delivery.services;


import com.example.parcel_delivery.models.entities.Cabinet;

import java.util.List;

public interface CabinetService {

    List<Cabinet> getAvailableCabinetsByParcelLockerLocationId(Long parcelLockerLocationId);

    Boolean isAvailableCabinet(Long parcelLockerLocationId);

    Cabinet reserveCabinetFromThe5Lockers(Long selectedLockerIdLong);

    
}
