package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelResDTO {


    private Long parcelId;
    private String status;
    private String senderName;
    // private String associatedStorage;
    private String recipientName;
    // private String pickupDateAndTime;
    // private String parcelDeliveryDateAndTime;
    private String parcelLockerLocationPoint;
    private String transactionCode;   
    private String codeExpiryDate;

}
