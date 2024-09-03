package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Parcel;

import java.util.List;

public interface CabinetService {

    List<Cabinet> getAvailableCabinetsByParcelLockerLocationId(Long parcelLockerLocationId);

    Boolean hasAnyAvailableCabinets(Long parcelLockerLocationId);

    Cabinet reserveCabinetFromThe5Lockers(Long selectedLockerIdLong);

    Cabinet holdCabinetForRecipientLocker(Long lockerId);

    Cabinet associateHeldCabinetWithParcel(Parcel parcel, Long lockerId);

    void save(Cabinet cabinet);

}
