package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelLockerResDTO {

    private String lockerId;
    private String name;
    private GeoPointDTO lockerPoint;
    

}
