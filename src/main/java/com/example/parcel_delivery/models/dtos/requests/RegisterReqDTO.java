package com.example.parcel_delivery.models.dtos.requests;

import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.models.enums.RegisterRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterReqDTO {

    @NotBlank(message = "username cannot be blank")
    private String username;

    @NotBlank(message = "password cannot be blank")
    private String password;

    @NotBlank(message = "firstname cannot be blank")
    private String firstname;

    @NotBlank(message = "surename cannot be blank")
    private String lastname;

    @NotBlank(message = "phone No. cannot be blank")
    private String phoneNumber;

    @NotBlank(message = "email cannot be blank")
    private String email;

    @Enumerated(EnumType.STRING)
    private  RegisterRole registerRole;

    private DriverType driverType; // This can be null for customers

}
