package com.example.parcel_delivery.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.requests.LoginReqDTO;
import com.example.parcel_delivery.models.dtos.requests.RegisterReqDTO;
import com.example.parcel_delivery.models.dtos.responses.LoginResDTO;
import com.example.parcel_delivery.models.dtos.responses.SignupResDTO;
import com.example.parcel_delivery.services.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResDTO> login(@Valid @RequestBody LoginReqDTO loginResquestDTO) throws Exception {
        return ResponseEntity.ok(authService.loginUser(loginResquestDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<SignupResDTO> register(@Valid @RequestBody RegisterReqDTO registerRequestDTO)
            throws Exception {
        return new ResponseEntity<>(authService.registerUser(registerRequestDTO), HttpStatus.CREATED);
    }

}
