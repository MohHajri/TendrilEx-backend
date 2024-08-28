package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.enums.CabinetStatus;
import com.example.parcel_delivery.repositories.CabinetRepo;
import com.example.parcel_delivery.services.CabinetService;

@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    private CabinetRepo cabinetRepo;

    @Override
    public List<Cabinet> getAvailableCabinetsByParcelLockerLocationId(Long parcelLockerLocationId) {
      return null; 
    }

    @Override
    public Boolean isAvailableCabinet(Long parcelLockerLocationId) {
        return null;
    }

    @Override
    public Cabinet reserveCabinetFromThe5Lockers(Long selectedLockerId) {
        Optional<Cabinet> freeCabinet = cabinetRepo.findRandomFreeCabinetByLockerId(selectedLockerId);
        if (freeCabinet.isPresent()) {
            Cabinet cabinet = freeCabinet.get();
            cabinet.setStatus(CabinetStatus.OCCUPIED);
            return cabinetRepo.save(cabinet);
        } else {
            throw new TendrilExExceptionHandler( HttpStatus.NOT_FOUND, "No free cabinets available in locker with id: " + selectedLockerId );
        }
        
        
    }

    @Override
    public void save(Cabinet cabinet) {
        cabinetRepo.save(cabinet);
        
    }
 
    
}
