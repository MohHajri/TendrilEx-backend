package com.example.parcel_delivery.services.impl;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;
import com.example.parcel_delivery.models.dtos.requests.LoginReqDTO;
import com.example.parcel_delivery.models.dtos.requests.RegisterReqDTO;
import com.example.parcel_delivery.models.dtos.responses.LoginResDTO;
import com.example.parcel_delivery.models.dtos.responses.SignupResDTO;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Role;
import com.example.parcel_delivery.models.entities.User;
import com.example.parcel_delivery.models.mappers.UserMapper;
import com.example.parcel_delivery.repositories.CustomerRepo;
import com.example.parcel_delivery.repositories.DriverRepo;
import com.example.parcel_delivery.repositories.RoleRepo;
import com.example.parcel_delivery.repositories.UserRepo;
import com.example.parcel_delivery.services.AuthService;
import com.example.parcel_delivery.services.CustomUserDetailService;
import com.example.parcel_delivery.utils.JWTUtils;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private DriverRepo driverRepository;

    @Autowired
    private CustomerRepo customerRepository;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        if (roleRepository.count() == 0) {
            Role admin = new Role();
            admin.setName("ROLE_ADMIN");
            roleRepository.save(admin);

            Role user = new Role();
            user.setName("ROLE_USER");
            roleRepository.save(user);
        }
    }

    @Override
    public SignupResDTO registerUser(RegisterReqDTO registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new TendrilExExceptionHandler(HttpStatus.CONFLICT, "A user with this username already exists");
        }
        // create new user
        User newUser = new User();
        // newUser.setUsername(registerDto.getUsername());
        newUser = userMapper.toUserEntity(registerDto);
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign default role to new user
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Role is not found."));
        newUser.setRoles(Set.of(userRole));

        userRepository.save(newUser);

        switch(registerDto.getRegisterRole()) {
            case DRIVER:
                Optional<Driver> existingDriver = driverRepository.findByUserId(newUser.getId());
                if(!existingDriver.isPresent()) {
                    Driver newDriver = new Driver();
                    newDriver.setUser(newUser);
                    driverRepository.save(newDriver);
                }

                break;

            case CUSTOMER:
               Optional <Customer> existingCustomer = customerRepository.findByUserId(newUser.getId());
               if(!existingCustomer.isPresent()) {
                    Customer newCustomer = new Customer();
                    newCustomer.setUser(newUser);
                    customerRepository.save(newCustomer);
                }

                break;
         }

        return SignupResDTO.builder()
                .message("User registered successfully")
                .build();
    }

    @Override
    public LoginResDTO loginUser(LoginReqDTO loginDto) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(loginDto.getUsername());
        return LoginResDTO.builder()
                .accessToken(jwtUtils.generateToken(userDetails))
                .build();
    }

}