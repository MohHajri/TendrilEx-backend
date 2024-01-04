package com.example.parcel_delivery.services;

import com.example.parcel_delivery.models.dtos.requests.LoginReqDTO;
import com.example.parcel_delivery.models.dtos.requests.RegisterReqDTO;
import com.example.parcel_delivery.models.dtos.responses.LoginResDTO;
import com.example.parcel_delivery.models.dtos.responses.SignupResDTO;

public interface AuthService {

    SignupResDTO registerUser(RegisterReqDTO registerReqDto);

    LoginResDTO loginUser(LoginReqDTO loginReqDto);

    boolean authenticateRobotUser();


}
