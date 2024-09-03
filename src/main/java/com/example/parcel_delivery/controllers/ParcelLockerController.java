package com.example.parcel_delivery.controllers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.mappers.ParcelLockerMapper;
import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.dtos.responses.ParcelLockerResDTO;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.services.ParcelLockerService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/parcel-lockers")
@AllArgsConstructor
public class ParcelLockerController {

    @Autowired
    private ParcelLockerService parcelLockerService;

    @Autowired
    private ParcelLockerMapper parcelLockerMapper;

    @GetMapping("/nearest5")
    public ResponseEntity<List<ParcelLockerResDTO>> getFiveNearestAvailablelockers(
            @RequestBody @Valid CustomerLocationReqDTO locationReqDTO) {

        List<ParcelLocker> lockers = parcelLockerService.getFiveNearestAvailableLockers(locationReqDTO);
        List<ParcelLockerResDTO> dtoList = lockers.stream()
                .map(parcelLockerMapper::toParcelLockerResDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

}
