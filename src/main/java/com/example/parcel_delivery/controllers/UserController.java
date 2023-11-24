package com.example.parcel_delivery.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parcel_delivery.models.dtos.responses.UserResDTO;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.mappers.UserMapper;
import com.example.parcel_delivery.services.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("id/{id}")
    public ResponseEntity<UserResDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserbyId(id);
        return ResponseEntity.ok(userMapper.toUserResDTO(user));
    }

    @GetMapping("username/{username}")
    public ResponseEntity<UserResDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserbyUsername(username);
        return ResponseEntity.ok(userMapper.toUserResDTO(user));
    }

    @GetMapping("authenticated")
    public ResponseEntity<UserResDTO> getAuthenticatedUser() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(userMapper.toUserResDTO(user));
    }
    
    
}
