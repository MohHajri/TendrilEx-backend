package com.example.parcel_delivery.models.dtos.responses;

import lombok.Data;

@Data
public class ParcelResDTO {


    private Long parcelId;
    private String status;
    private String senderName;
    private String recipientName;;
    private String parcelLockerLocationPoint;
    private String transactionCode;   
    private String codeExpiryDate;

}
