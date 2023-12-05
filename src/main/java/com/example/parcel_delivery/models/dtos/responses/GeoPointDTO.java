package com.example.parcel_delivery.models.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoPointDTO {
    private double latitude;
    private double longitude;
}
