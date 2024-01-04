package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.repositories.ParcelLockerRepo;
import com.example.parcel_delivery.services.CustomerService;
import com.example.parcel_delivery.services.ParcelLockerService;

@Service
public class ParcelLockerServiceImpl implements ParcelLockerService {

    @Autowired
    private ParcelLockerRepo parcelLockerRepo;

    @Autowired 
    private CustomerService customerService;

    @Override
    public List<ParcelLocker> getFiveNearestAvailablelockers() {

        Customer customer = customerService.getCustomerByAuthenticatedUser();
        Point senderPoint = customer.getUser().getUserPoint();
        List<ParcelLocker> nearestAvailableLockers = parcelLockerRepo.getFiveNearestAvailablelockers(senderPoint);

        return nearestAvailableLockers.stream().limit(5).collect(Collectors.toList());
    }

    @Override
    public ParcelLocker getParcelLockerById(Long selectedLockerId) {
        return parcelLockerRepo.
                findById(selectedLockerId).
                orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No locker found with id: " + selectedLockerId));
    }
}
