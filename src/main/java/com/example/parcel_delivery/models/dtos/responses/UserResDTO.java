package com.example.parcel_delivery.models.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResDTO {

    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    
    
}
