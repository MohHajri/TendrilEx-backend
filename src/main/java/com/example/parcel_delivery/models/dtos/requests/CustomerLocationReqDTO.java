package com.example.parcel_delivery.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerLocationReqDTO {

    @NotBlank(message = "Recipient address is mandatory")
    private String customerAddress;

    @NotBlank(message = "Recipient postcode is mandatory")
    @Pattern(regexp = "\\d{5}", message = "Invalid postcode")
    private String customerPostcode;

    @NotBlank(message = "Recipient city is mandatory")
    private String customerCity;

}
