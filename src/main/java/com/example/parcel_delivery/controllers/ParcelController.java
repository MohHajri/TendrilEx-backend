package com.example.parcel_delivery.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.responses.ParcelResDTO;
import com.example.parcel_delivery.models.mappers.ParcelMapper;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/parcels")
@AllArgsConstructor
public class ParcelController {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private ParcelMapper parcelMapper;

    @PostMapping("/send")
    public ResponseEntity<ParcelResDTO> sendNewParcel(@RequestBody ParcelReqDTO request) {
        return ResponseEntity
                .ok(parcelMapper
                        .toParcelResDTO(parcelService
                                .sendNewParcel(request)));
    }
    
}
