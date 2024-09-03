package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.CabinetStatus;
import com.example.parcel_delivery.repositories.CabinetRepo;
import com.example.parcel_delivery.services.CabinetService;

@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    private CabinetRepo cabinetRepo;

    /**
     * Retrieves a free cabinet from a specified parcel lokcker
     * 
     * @param parcelLockerId
     * @return
     */
    @Override
    public List<Cabinet> getAvailableCabinetsByParcelLockerLocationId(Long parcelLockerId) {
        return cabinetRepo.findAvailableCabinetsByLockerId(parcelLockerId);
    }

    /**
     * Checks if a specified lokcer has some free slots ( cabinets)
     * 
     * @param parcelLockerId
     * @return
     */
    @Override
    public Boolean hasAnyAvailableCabinets(Long parcelLockerId) {
        return !cabinetRepo.findAvailableCabinetsByLockerId(parcelLockerId).isEmpty();
    }

    /**
     * It finds a random free cabinet from the selected parcel locker and then it
     * reserves that cabinet (it sets it OCCUPIED )
     * It means now the cabinet is assoicated with the parcel
     * 
     * @param selectedLockerId
     * @return
     */
    @Override
    public Cabinet reserveCabinetFromThe5Lockers(Long selectedLockerId) {
        Optional<Cabinet> freeCabinet = cabinetRepo.findRandomFreeCabinetByLockerId(selectedLockerId);
        if (freeCabinet.isPresent()) {
            Cabinet cabinet = freeCabinet.get();
            cabinet.setStatus(CabinetStatus.OCCUPIED);
            return cabinetRepo.save(cabinet);
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                    "No free cabinets available in locker with id: " + selectedLockerId);
        }
    }

    /**
     * It finds a random free cabinet from the selected parcel locker and then it
     * holds that cabinet (it sets it RESERVED )
     * it is used to hold (temporarily) the cabinet in the recipient area (if pickup
     * point
     * delivery chosen)
     * 
     * @param lockerId
     * @return
     */
    @Override
    public Cabinet holdCabinetForRecipientLocker(Long lockerId) {
        Optional<Cabinet> freeCabinet = cabinetRepo.findRandomFreeCabinetByLockerId(lockerId);
        if (freeCabinet.isPresent()) {
            Cabinet cabinet = freeCabinet.get();
            cabinet.setStatus(CabinetStatus.RESERVED);
            return cabinetRepo.save(cabinet);
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                    "No free cabinets available in locker with id: " + lockerId);
        }
    }

    /**
     * it sets the cabinet now as not available and associates it with the parcel
     * it is used when we decide to ship the parcel to the recipient chosen pickup
     * address (parcel locker)
     * 
     * @param lockerId
     * @param parcel
     * 
     * @return
     */
    @Override
    public Cabinet associateHeldCabinetWithParcel(Parcel parcel, Long lockerId) {
        Optional<Cabinet> heldCabinet = cabinetRepo.findHeldCabinetByLockerId(lockerId);
        if (heldCabinet.isPresent()) {
            Cabinet cabinet = heldCabinet.get();
            cabinet.setStatus(CabinetStatus.OCCUPIED);
            cabinet.setCurrentParcel(parcel);
            return cabinetRepo.save(cabinet);
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                    "No held cabinets available in locker with id: " + lockerId);
        }
    }

    @Override
    public void save(Cabinet cabinet) {
        cabinetRepo.save(cabinet);

    }

}
