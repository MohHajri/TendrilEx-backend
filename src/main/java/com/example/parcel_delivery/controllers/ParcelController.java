package com.example.parcel_delivery.controllers;


import java.util.List;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.responses.ParcelResDTO;
import com.example.parcel_delivery.models.entities.Parcel;
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

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResDTO> getParcelById(@PathVariable Long id) {
        return ResponseEntity
                .ok(parcelMapper
                        .toParcelResDTO(parcelService
                                .getParcelById(id)));
    }

    @GetMapping("id/{id}/sender/{senderId}")
    public ResponseEntity<ParcelResDTO> getParcelByParcelIdAndSenderId(@PathVariable Long id, @PathVariable Long senderId) {
        return ResponseEntity
                .ok(parcelMapper
                        .toParcelResDTO(parcelService
                                .getByParcelIdAndSenderId(id, senderId)));
    }

    @GetMapping("id/{id}/recipient/{recipientId}")
    public ResponseEntity<ParcelResDTO> getParcelByParcelIdAndRecipientId(@PathVariable Long id, @PathVariable Long recipientId) {
        return ResponseEntity
                .ok(parcelMapper
                        .toParcelResDTO(parcelService
                                .getByParcelIdAndRecipientId(id, recipientId)));
    }

    @GetMapping("/sender/{id}/sent")
    public ResponseEntity<List<ParcelResDTO>> getSentParcelsByCustomerId(@PathVariable Long id) {

        List<Parcel> parcels = parcelService.getSentParcelsByCustomerId(id);
        List<ParcelResDTO> dtoList = parcels.stream()
                                            .map(parcelMapper::toParcelResDTO)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
     
    }

    @GetMapping("/recipient/{id}/received")
    public ResponseEntity<List<ParcelResDTO>> getReceivedParcelsByCustomerId(@PathVariable Long id) {

        List<Parcel> parcels = parcelService.getReceivedParcelsByCustomerId(id);
        List<ParcelResDTO> dtoList = parcels.stream()
                                            .map(parcelMapper::toParcelResDTO)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    
}
