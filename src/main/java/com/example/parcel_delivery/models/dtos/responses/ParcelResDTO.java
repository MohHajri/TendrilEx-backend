package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelResDTO {

    private Long parcelId;
    private String status;
    private String senderName;
    private String recipientName;;
    private String parcelLockerLocationPoint;
    private String senderTransactionCode;
    private String codeExpiryDate;
    private String parcelLockerLocationName;
    private String cabinetNumber;

}

// as for now the location of the parcel locker is a point . so in an atucal
// setting we ahve to return the raw actual location , not the Point