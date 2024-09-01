package com.example.parcel_delivery.models.dtos.responses;

import com.example.parcel_delivery.models.enums.DriverType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DriverResDTO {

    private Long userId;
    private Long driverId;
    private String username;
    private Boolean isAvailable;
    private DriverType driverType;
    private String city;

}
