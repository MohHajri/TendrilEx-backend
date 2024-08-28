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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.stereotype.Service;


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

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ROBOT_USERNAME_HELSINKI = "robotUserHelsinki";
    private static final String ROBOT_USERNAME_OULU = "robotUserOulu";
    private static final String ROBOT_PASSWORD = "123456";


    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        initializeRolesAndRobotUsers();
    }

    private void initializeRolesAndRobotUsers() {
        Set<String> existingRoles = new HashSet<>(roleRepository.findAllRoleNames());
        for (String roleName : Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_ROBOT")) {
            if (!existingRoles.contains(roleName)) {
                roleRepository.save(new Role(null, roleName));
            }
        }
    
        createRobotUserIfNotExists(ROBOT_USERNAME_HELSINKI, "Helsinki", new Coordinate(24.945831, 60.192059));
        createRobotUserIfNotExists(ROBOT_USERNAME_OULU, "Oulu", new Coordinate(25.46816, 65.01236));
    }

    private void createRobotUserIfNotExists(String username, String city, Coordinate location) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User robotUser = new User();
            robotUser.setUsername(username);
            robotUser.setPassword(passwordEncoder.encode(ROBOT_PASSWORD));
            robotUser.setCity(city);
            robotUser.setUserPoint(new GeometryFactory().createPoint(location));
            robotUser.setRoles(Set.of(roleRepository.findByName("ROLE_ROBOT")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ROBOT not found"))));
            userRepository.save(robotUser);
            createOrUpdateCustomer(robotUser);
        }
    }

    
    @Override
    public SignupResDTO registerUser(RegisterReqDTO registerDto) {
        User user = userRepository.findByUsername(registerDto.getUsername())
                    .orElseGet(() -> createUser(registerDto));
    
        switch (registerDto.getRegisterRole()) {
            case DRIVER:
                createOrUpdateDriver(registerDto, user);
                break;
            case CUSTOMER:
                createOrUpdateCustomer(user);
                break;
            default:
                throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Invalid registration role");
        }
    
        return SignupResDTO.builder().message("User registered successfully").build();
    }

    private User createUser(RegisterReqDTO registerDto) {
        User newUser = userMapper.toUserEntity(registerDto);
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
    
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "Role is not found."));
        newUser.setRoles(Set.of(userRole));
    
        return userRepository.save(newUser);
    }

    private void createOrUpdateDriver(RegisterReqDTO registerDto, User user) {
        if (driverRepository.findByUserId(user.getId()).isPresent()) {
            throw new TendrilExExceptionHandler(HttpStatus.CONFLICT, "This user is already registered as a driver");
        }
        
        if (registerDto.getDriverType() == null) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver type is required for driver registration");
        }
        
        Driver newDriver = new Driver();
        newDriver.setUser(user);
        newDriver.setDriverType(registerDto.getDriverType());
        newDriver.setIsAvailable(true);
        driverRepository.save(newDriver);
    }

    private void createOrUpdateCustomer(User user) {
        if (customerRepository.findByUserId(user.getId()).isPresent()) {
            throw new TendrilExExceptionHandler(HttpStatus.CONFLICT, "This user is already registered as a customer");
        }
        
        Customer newCustomer = new Customer();
        newCustomer.setUser(user);
        customerRepository.save(newCustomer);
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

    @Override
    public boolean authenticateRobotUser(String robotUsername) {
        try {
            // Use the provided robotUsername for authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    robotUsername, ROBOT_PASSWORD);
            SecurityContextHolder.getContext().setAuthentication(
                    authenticationManager.authenticate(authentication));
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }    

}