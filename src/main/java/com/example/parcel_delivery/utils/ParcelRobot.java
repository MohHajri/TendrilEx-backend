package com.example.parcel_delivery.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.repositories.CustomerRepo;
import com.example.parcel_delivery.services.AuthService;
import com.example.parcel_delivery.services.ParcelService;

import java.util.List;
import java.util.Random;
@Component
public class ParcelRobot {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerRepo customerRepo;

    private Random random = new Random();
    

    @EventListener(ApplicationReadyEvent.class)
    public void sendParcelsPeriodically() {
        if (authService.authenticateRobotUser()) {
            List<Customer> customers = customerRepo.findAll();
            if (!customers.isEmpty()) {
                sendRandomParcels(10, customers);
            }
        }
    }

    public void sendRandomParcels(int numberOfParcels, List<Customer> customers) {

        for (int i = 0; i < numberOfParcels; i++) {
            ParcelReqDTO dto = createRandomParcelReqDTO(customers);
            try {
                parcelService.sendNewParcel(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // simple robot to generate random parcels mimicking the e commerce
    private ParcelReqDTO createRandomParcelReqDTO(List<Customer> customers){
        ParcelReqDTO dto = new ParcelReqDTO();
        if (customers.isEmpty()) {
            throw new IllegalStateException("No registered customers available for parcel assignment");
        }
    
        // Select a random recipient from existing registered customers
        Customer randomRecipient = customers.get(random.nextInt(customers.size()));
    
        // Set DTO fields for sender (robot)
        dto.setSenderName("robotUser");
        dto.setSenderLatitude("60.192059"); // Fixed latitude for Helsinki
        dto.setSenderLongitude("24.945831"); // Fixed longitude for Helsinki
        dto.setSenderCity("Helsinki");
        dto.setSenderPhoneNo("0401234567");
        dto.setSenderEmail("sender@example.com");
    
        // Set recipient details based on the randomly selected registered customer
        dto.setRecipientName(randomRecipient.getUser().getFirstName() + " " + randomRecipient.getUser().getLastName());
        dto.setRecipientAddress(randomRecipient.getUser().getAddress());
        dto.setRecipientPostcode(randomRecipient.getUser().getPostcode());
        dto.setRecipientCity(randomRecipient.getUser().getCity());
        dto.setRecipientPhoneNo(randomRecipient.getUser().getPhoneNumber());
        dto.setRecipientEmail(randomRecipient.getUser().getEmail());
    
        // Parcel details
        dto.setWeight(10.0);
        dto.setWidth(30.0);
        dto.setHeight(20.0);
        dto.setDepth(15.0);
        dto.setMass(5.0);
        dto.setDescription("Parcel Description " + random.nextInt(1000));
        dto.setSelectedLockerId(1L);
        dto.setDropOffLatitude("60.192059");
        dto.setDropOffLongitude("24.945831");
        dto.setIdempotencyKey("ID-" + random.nextInt(10000));
    
        return dto;
    }    

    
}
