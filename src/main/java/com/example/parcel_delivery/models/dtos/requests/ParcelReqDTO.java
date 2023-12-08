package com.example.parcel_delivery.models.dtos.requests;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParcelReqDTO {

    @NotBlank(message = "Sender name is mandatory")
    private String senderName;

    @NotBlank(message = "Sender Latitude is mandatory")
    private String senderLatitude;

    @NotBlank(message = "Sender Longitude is mandatory")
    private String senderLongitude;

    @NotBlank(message = "Sender phone is mandatory")
    @Pattern(regexp = "^04\\d{7,8}$|^050\\d{6,7}$", message = "Invalid Finnish mobile phone number.")
    private String senderPhoneNo;
    
    @NotBlank(message = "Recipient email is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String senderEmail;

    @NotBlank(message = "Recipient name is mandatory")
    private String recipientName;

    @NotBlank(message = "Recipient address is mandatory")
    private String recipientAddress;

    @NotBlank(message = "Recipient postcode is mandatory")
    @Pattern(regexp = "\\d{5}", message = "Invalid postcode")
    private String recipientPostcode;

    @NotBlank(message = "Recipient city is mandatory")
    private String recipientCity;

    @NotBlank(message = "recipient phone is mandatory")
    @Pattern(regexp = "^04\\d{7,8}$|^050\\d{6,7}$", message = "Invalid Finnish mobile phone number.")
    private String recipientPhoneNo;

    @NotBlank(message = "Recipient email is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String recipientEmail;

    @NotBlank(message = "Parcel weight is mandatory")
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0.1")
    @DecimalMax(value = "100", message = "Weight must be less than 100")
    private Double weight; //  in kg

    @NotBlank(message = "Parcel width is mandatory")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Invalid width format")
    private Double width; // in cm

    @NotBlank(message = "Parcel height is mandatory")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Invalid height format")
    private Double height; // in cm

    @NotBlank(message = "Parcel depth is mandatory")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Invalid depth format")
    private Double depth; // in cm
 
    @NotBlank(message = "Parcel mass is mandatory") 
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Invalid mass format")
    private Double mass;

    @NotBlank(message = "Parcel description is mandatory")
    private String description;

    @NotBlank(message = "Selected Locker ID is mandatory")
    private Long selectedLockerId;  

    @NotBlank(message = "DropOff Latitude is mandatory")
    private String dropOffLatitude;

    @NotBlank(message = "DropOff Longitude is mandatory")
    private String dropOffLongitude;

    @NotBlank(message = "idempotencyKey is mandatory")
    private String idempotencyKey;



}
