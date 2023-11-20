package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelResDTO {


    private Long parcelId;
    private String parcelStatus;
    private String parcelSender;
    private String parcelRecipient;
    private String parcelAtCabinetDate;
    private String parcelPickupDate;
    private String parcelDeliveryDate;
    private String parcelLockerLocation;
    private String CabinetCode;   

}
