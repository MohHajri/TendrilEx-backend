package com.example.parcel_delivery.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.repositories.CustomerRepo;
import com.example.parcel_delivery.repositories.ParcelLockerRepo;
import com.example.parcel_delivery.services.AuthService;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.services.impl.BatchParcelAssignmentServiceImpl;
import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.HashSet;
import java.util.Set;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Order(2)  // Lower will run after loader 
public class ParcelRobot {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ParcelLockerRepo parcelLockerRepo;

    @Autowired
    private BatchParcelAssignmentServiceImpl batchParcelAssignmentService; // Inject Batch Service



    private Random random = new Random();
    private static final Logger logger = Logger.getLogger(ParcelRobot.class.getName());

    // Track customers who have already received parcels from a robot
    private Set<Long> helsinkiSentCustomers = new HashSet<>();
    private Set<Long> ouluSentCustomers = new HashSet<>();

    /**
     * This method is triggered when the application is ready.
     * It authenticates the robot users and triggers parcel generation for each robot if the authentication is successful.
     */
    @EventListener(ApplicationReadyEvent.class) 
    public void sendParcelsPeriodically() {
        try {
            System.err.println("beginging of parcel robot");
            // Authenticate and send parcels for both Helsinki and Oulu robots
            if (authService.authenticateRobotUser("robotUserHelsinki")) {
                sendParcelsForCity("robotUserHelsinki", "Helsinki", helsinkiSentCustomers);
                sendParcelsForCity("robotUserHelsinki", "Oulu", helsinkiSentCustomers);
            }
            if (authService.authenticateRobotUser("robotUserOulu")) {
                sendParcelsForCity("robotUserOulu", "Oulu", ouluSentCustomers);
                sendParcelsForCity("robotUserOulu", "Helsinki", ouluSentCustomers);
            }

            // After parcel robot completes, trigger batch processing
            batchParcelAssignmentService.batchAssignParcels();

        } catch (Exception e) {
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start parcel robot: " + e.getMessage());
        }
    }

    /**
     * Sends parcels for a specific robot user in a given city.
     *
     * @param robotUsername The robot username
     * @param city The city to which the robot sends parcels
     */
    private void sendParcelsForCity(String robotUsername, String city, Set<Long> sentCustomers) {
        int page = 0;
        int pageSize = 20;
        Page<Customer> customerPage;

        do {
            Pageable pageable = PageRequest.of(page, pageSize);
            // customerPage = customerRepo.findByUserCity(city, pageable);
            customerPage = customerRepo.findByUserCityAndRole(pageable, city);

            List<Customer> customers = customerPage.getContent();

            if (!customers.isEmpty()) {
                // Handle multiple results appropriately
                if (customers.size() > 1) {
                    logger.log(Level.INFO, "Processing " + customers.size() + " customers in " + city + ".");
                }
                sendRandomParcels(customers, robotUsername, sentCustomers);
            } else {
                logger.log(Level.WARNING, "No registered customers available in " + city);
            }

            page++;
        } while (customerPage.hasNext()); // Continue until no more customers
    }

    /**
     * Sends random parcels to a list of customers.
     * Each parcel is generated with random attributes and sent to a randomly selected customer.
     * 
     * @param customers the list of customers to which parcels can be sent
     * @param robotUsername the robot user sending the parcels
     */
    public void sendRandomParcels(List<Customer> customers, String robotUsername, Set<Long> sentCustomers) {
        for (Customer customer : customers) {
            if (sentCustomers.contains(customer.getId())) {
                // Skip customers who have already received a parcel from this robot
                continue;
            }

            // Create a random parcel request for each customer
            ParcelReqDTO dto = createRandomParcelReqDTO(customer, robotUsername);
            try {

                //TEMPORARY
                // // Attempt to send the parcel
                // parcelService.sendNewParcel(dto);

                //TEST 

                Parcel parcel = parcelService.sendNewParcel(dto);
                // Simulate the drop-off process to update the status to 'AWAITING_DRIVER_ASSIGNMENT'
                 parcelService.dropOffParcelInCabinet(parcel.getId(), parcel.getTransactionCode());

                //TEST 


                // Mark this customer as having received a parcel from this robot
                sentCustomers.add(customer.getId());
            } catch (Exception e) {
                // Retry if sending fails
                handleParcelSendingFailure(dto, e);
            }
        }
    }

    /**
     * Handles failures in sending parcels by retrying up to 3 times with exponential backoff.
     * 
     * @param dto the parcel request that failed to send
     * @param e the exception that caused the failure
     */
    private void handleParcelSendingFailure(ParcelReqDTO dto, Exception e) {
        int attempts = 0;
        boolean success = false;
        while (attempts < 3 && !success) {
            try {
                // Exponential backoff before retrying
                Thread.sleep((long) Math.pow(2, attempts) * 1000);
                parcelService.sendNewParcel(dto);
                success = true;
            } catch (Exception retryException) {
                attempts++;
                if (attempts >= 3) {
                    throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send parcel after retries: " + retryException.getMessage());
                }
            }
        }
    }

    /**
     * Creates a random ParcelReqDTO object to simulate sending a parcel.
     * The recipient is randomly selected from the list of customers, either in Helsinki or Oulu.
     * An available locker and cabinet are selected for each parcel.
     * 
     * @param customer the customer to whom the parcel will be sent
     * @param robotUsername the robot user sending the parcel
     * @return a ParcelReqDTO object populated with random data
     */
    private ParcelReqDTO createRandomParcelReqDTO(Customer customer, String robotUsername) {
        ParcelReqDTO dto = new ParcelReqDTO();
    
        // Set DTO fields for sender (robot)
        dto.setSenderName(robotUsername);
        
        Point senderLocation;
        if (robotUsername.equals("robotUserHelsinki")) {
            senderLocation = new GeometryFactory().createPoint(new org.locationtech.jts.geom.Coordinate(24.945831, 60.192059)); // Helsinki location
        } else if (robotUsername.equals("robotUserOulu")) {
            senderLocation = new GeometryFactory().createPoint(new org.locationtech.jts.geom.Coordinate(25.46816, 65.01236)); // Oulu location
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown robot user");
        }
        senderLocation.setSRID(4326); // Ensure SRID is set correctly
        
        dto.setSenderLatitude(String.valueOf(senderLocation.getY()));
        dto.setSenderLongitude(String.valueOf(senderLocation.getX()));
        dto.setSenderCity(customer.getUser().getCity());
        dto.setSenderPhoneNo("0401234567");
        dto.setSenderEmail("sender@example.com");
    
        // Set recipient details based on the provided customer
        dto.setRecipientName(customer.getUser().getFirstName() + " " + customer.getUser().getLastName());
        dto.setRecipientAddress(customer.getUser().getAddress());
        dto.setRecipientPostcode(customer.getUser().getPostcode());
        dto.setRecipientCity(customer.getUser().getCity());
        dto.setRecipientPhoneNo(customer.getUser().getPhoneNumber());
        dto.setRecipientEmail(customer.getUser().getEmail());
    
        // Parcel details
        dto.setWeight(10.0);
        dto.setWidth(30.0);
        dto.setHeight(20.0);
        dto.setDepth(15.0);
        dto.setMass(5.0);
        dto.setDescription("Parcel Description " + random.nextInt(1000));
        dto.setDropOffLatitude(String.valueOf(senderLocation.getY()));
        dto.setDropOffLongitude(String.valueOf(senderLocation.getX()));
        dto.setIdempotencyKey("ID-" + random.nextInt(10000));
    
        // Select an available locker and cabinet
        List<ParcelLocker> availableLockers = parcelLockerRepo.getFiveNearestAvailablelockers(senderLocation);
        
        // Ensure the list of lockers is unique
        Set<ParcelLocker> uniqueLockers = new HashSet<>(availableLockers);
        
        if (!uniqueLockers.isEmpty()) {
            // Randomly select one of the nearest available lockers
            ParcelLocker selectedLocker = uniqueLockers.stream().skip(random.nextInt(uniqueLockers.size())).findFirst().orElse(null);
            dto.setSelectedLockerId(selectedLocker.getId());
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No available lockers found");
        }
    
        // Validate parcel data before sending
        validateParcelData(dto);
    
        return dto;
    }    


    /**
     * Validates the parcel data to ensure that all measurements and weights are positive.
     * If any data is invalid, an exception is thrown.
     * 
     * @param dto the ParcelReqDTO to be validated
     */
    private void validateParcelData(ParcelReqDTO dto) {
        if (dto.getWeight() <= 0 || dto.getWidth() <= 0 || dto.getHeight() <= 0 || dto.getDepth() <= 0 || dto.getMass() <= 0) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Invalid parcel data: " + dto.toString());
        }
    }
}
