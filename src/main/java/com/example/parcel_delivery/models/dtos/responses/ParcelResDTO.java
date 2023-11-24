package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelResDTO {


    private Long parcelId;
    private String parcelStatus;
    private String parcelSenderName;
    private String parcelAssociatedStorage;
    private String parcelRecipientName;
    private String parcelPickupDateAndTime;
    private String parcelDeliveryDateAndTime;
    private String parcelLockerLocation;
    private String parcelCode;   

}
