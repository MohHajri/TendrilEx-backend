package com.example.parcel_delivery.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.repositories.ParcelLockerRepo;
import com.example.parcel_delivery.services.CustomerService;
import com.example.parcel_delivery.services.ParcelLockerService;
import com.example.parcel_delivery.utils.LocationUtils;

@Service
public class ParcelLockerServiceImpl implements ParcelLockerService {

    @Autowired
    private ParcelLockerRepo parcelLockerRepo;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LocationUtils locationUtil;

    /**
     * gets the 5 nearest available parcel lockers to the customer logged in
     * It uses PostGis operations to do so. Interesting, isn't it?
     * Not used now
     * 
     * @param parcelLockerId
     * @return
     */
    @Override
    public List<ParcelLocker> getFiveNearestAvailablelockers() {

        Customer customer = customerService.getCustomerByAuthenticatedUser();
        Point senderPoint = customer.getUser().getUserPoint();
        List<ParcelLocker> nearestAvailableLockers = parcelLockerRepo.getFiveNearestAvailablelockers(senderPoint);

        return nearestAvailableLockers.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific parcel locker bu its id
     * 
     * @param parcelLockerId
     * @return
     */
    @Override
    public ParcelLocker getParcelLockerById(Long selectedLockerId) {
        return parcelLockerRepo.findById(selectedLockerId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "No locker found with id: " + selectedLockerId));
    }

    /**
     * this method returns the five nearest parcel lockers to either the sender or
     * the receiver upon calling.
     * * It uses PostGis operations to do so. Interesting, isn't it?
     * 
     * it will be used by the parcel sender when they want to select the closest
     * parcel lokcer to them to drop off the parcel for dirver
     * or (if applicable) to select the closest parcel lokcer to receive they want
     * to send to.
     * 
     * @param locationReqDTO
     * @return
     */
    @Override
    public List<ParcelLocker> getFiveNearestAvailableLockers(CustomerLocationReqDTO locationReqDTO) {
        // Convert the DTO to a location point
        Point locationPoint = locationUtil.geocodeLocation(locationReqDTO);

        System.out.println(
                "Geocoded Point: Latitude = " + locationPoint.getY() + ", Longitude = " + locationPoint.getX());

        // Fetch the nearest lockers based on the calculated location point
        List<ParcelLocker> nearestAvailableLockers = parcelLockerRepo.getFiveNearestAvailablelockers(locationPoint);

        // Return only the top 5 nearest lockers
        return nearestAvailableLockers.stream().limit(5).collect(Collectors.toList());
    }

}
