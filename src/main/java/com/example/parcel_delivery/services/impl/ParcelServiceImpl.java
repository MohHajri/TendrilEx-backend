package com.example.parcel_delivery.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.parcel_delivery.models.dtos.requests.CustomerLocationReqDTO;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Driver;
import com.example.parcel_delivery.models.entities.Parcel;
// import com.example.parcel_delivery.models.entities.ParcelLocker;
import com.example.parcel_delivery.models.entities.Storage;
import com.example.parcel_delivery.models.enums.CabinetStatus;
import com.example.parcel_delivery.models.enums.DriverType;
import com.example.parcel_delivery.models.enums.NotificationType;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.models.enums.ParcelType;
import com.example.parcel_delivery.repositories.ParcelRepo;
import com.example.parcel_delivery.services.CabinetService;
import com.example.parcel_delivery.services.CustomerService;
import com.example.parcel_delivery.services.DriverService;
import com.example.parcel_delivery.services.NotificationService;
import com.example.parcel_delivery.services.ParcelLockerService;
import com.example.parcel_delivery.services.ParcelService;
import com.example.parcel_delivery.services.StorageService;
import com.example.parcel_delivery.utils.LocationUtils;
import com.example.parcel_delivery.utils.TransactionCodeGenerator;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;

import com.example.parcel_delivery.exceptions.TendrilExExceptionHandler;

@Service
public class ParcelServiceImpl implements ParcelService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ParcelLockerService ParcelLockerService;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LocationUtils locationUtil;

    @Autowired
    private TransactionCodeGenerator transactionCodeGenerator;

    @Autowired
    private ParcelRepo parcelRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private DriverService driverService;

    // @Autowired
    // private ParcelLockerService parcelLockerService;

    @Override
    public Parcel sendNewParcel(ParcelReqDTO parcelReqDTO) {
        try {
            // Step 1: Check if the parcel is already sent (Idempotency)
            Optional<Parcel> existingParcel = parcelRepository.findByIdempotencyKey(parcelReqDTO.getIdempotencyKey());
            if (existingParcel.isPresent()) {
                return existingParcel.get();
            }

            // Get authenticated customer and act as the sender
            Customer sender = customerService.getCustomerByAuthenticatedUser();
            if (sender == null) {
                throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN,
                        "You are not allowed to access this resource");
            }

            // Step 2: Find recipient customer based on phone number
            Optional<Customer> recipientOpt = customerService
                    .findCustomerByPhoneNumber(parcelReqDTO.getRecipientPhoneNo());
            Customer recipient = null;
            boolean isRecipientRegistered = false;

            if (recipientOpt.isPresent()) {
                recipient = recipientOpt.get();
                isRecipientRegistered = true;
            }

            // Temporary comments this off
            // // Step 4: Geocode the sender's address to get location
            // CustomerLocationReqDTO senderLocationDTO = new CustomerLocationReqDTO(
            // parcelReqDTO.getSenderAddress(), parcelReqDTO.getSenderPostcode(),
            // parcelReqDTO.getSenderCity());
            // Point senderLocation = locationUtil.geocodeLocation(senderLocationDTO);
            // sender.getUser().setUserPoint(senderLocation);

            // Step 5: Determine the Parcel Type (Intra-city or Inter-city)
            ParcelType parcelType = determineParcelType(parcelReqDTO.getSenderCity(), parcelReqDTO, recipient);

            // Step 6: Reserve a Cabinet in the selected locker (sender's locker)
            Cabinet reservedCabinet = cabinetService
                    .reserveCabinetFromThe5Lockers(parcelReqDTO.getSelectedSenderLockerId());

            // Step 7: Hold a cabinet in the recipient's locker (if applicable)
            if (parcelReqDTO.getIsDeliverToRecipientLocker() && parcelReqDTO.getSelectedRecipientLockerId() != null) {
                cabinetService.holdCabinetForRecipientLocker(parcelReqDTO.getSelectedRecipientLockerId());
            }

            // Step 8: Generate Transaction Code for the sender( will be useed by intra
            // driver as wll for pickup)
            Integer transactionCode = transactionCodeGenerator.generateTransactionCode();

            // Step 9: Create the Parcel entity and populate with details
            Parcel parcel = new Parcel();
            parcel.setSender(sender);
            parcel.setDepth(parcelReqDTO.getDepth());
            parcel.setHeight(parcelReqDTO.getHeight());
            parcel.setWidth(parcelReqDTO.getWidth());
            parcel.setMass(parcelReqDTO.getMass());
            parcel.setDescription(parcelReqDTO.getDescription());
            parcel.setParcelType(parcelType);
            parcel.setStatus(ParcelStatus.CREATED);
            parcel.setCabinet(reservedCabinet);
            parcel.setSelectedLockerLocation(
                    ParcelLockerService.getParcelLockerById(parcelReqDTO.getSelectedSenderLockerId()));
            parcel.setSenderTransactionCode(transactionCode);
            parcel.setSenderTransactionCodeValidUntil(LocalDateTime.now().plusDays(12));
            parcel.setIdempotencyKey(parcelReqDTO.getIdempotencyKey());
            parcel.setIdempotencyKeyCreatedAt(LocalDateTime.now());
            parcel.setIsRecipientRegistered(isRecipientRegistered);

            // Step 10: If the recipient is registered, set the recipient details
            if (isRecipientRegistered) {
                parcel.setRecipient(recipient);
            } else {
                // If recipient is not registered, set the unregistered recipient details
                parcel.setUnregisteredRecipientName(parcelReqDTO.getRecipientName());
                parcel.setUnregisteredRecipientPhone(parcelReqDTO.getRecipientPhoneNo());
                parcel.setUnregisteredRecipientEmail(parcelReqDTO.getRecipientEmail());
                parcel.setUnregisteredRecipientAddress(parcelReqDTO.getRecipientAddress());
                parcel.setUnregisteredRecipientPostcode(parcelReqDTO.getRecipientPostcode());
                parcel.setUnregisteredRecipientCity(parcelReqDTO.getRecipientCity());
            }

            // Set it true that recipient has a pickup point delivery; not home delivery
            if (parcelReqDTO.getIsDeliverToRecipientLocker()) {
                parcel.setDeliverToRecipientLocker(true);
            }

            // Set parcel in cabinet
            reservedCabinet.setCurrentParcel(parcel);

            // Step 11: Save parcel
            Parcel savedParcel = parcelRepository.save(parcel);

            // Step 12: Send notifications
            if (recipient != null && recipient.getUser() != null) {
                // Registered recipient
                notificationService.sendInAppNotification(
                        savedParcel,
                        NotificationType.NEW_PARCEL,
                        "You have a new parcel",
                        recipient.getUser());

                notificationService.sendEmailNotification(
                        recipient.getUser().getEmail(),
                        transactionCode,
                        "New Parcel",
                        recipient.getUser().getFirstName(),
                        ParcelStatus.CREATED);
            } else {
                // Unregistered recipient
                notificationService.sendEmailNotification(
                        parcelReqDTO.getRecipientEmail(),
                        transactionCode,
                        "New Parcel",
                        parcelReqDTO.getRecipientName(),
                        ParcelStatus.CREATED);
            }

            return savedParcel;

        } catch (DataIntegrityViolationException e) {
            // This will catch unique constraint violations
            throw new TendrilExExceptionHandler(HttpStatus.CONFLICT,
                    "Parcel with this idempotency key already exists.");
        }
    }

    // Updated helper
    private ParcelType determineParcelType(String senderCity, ParcelReqDTO parcelReqDTO, Customer recipient) {
        String recipientCity;

        // If the recipient is registered, use their city; otherwise, use the city from
        // ParcelReqDTO
        if (recipient != null && recipient.getUser() != null) {
            recipientCity = recipient.getUser().getCity();
        } else {
            recipientCity = parcelReqDTO.getRecipientCity(); // Fallback to the city provided in the request DTO
        }

        return senderCity.equals(recipientCity) ? ParcelType.INTRA_CITY : ParcelType.INTER_CITY;
    }

    /**
     * Retrieves a specific parcel by its id
     * 
     * @param parcelId
     * @return
     */
    @Override
    public Parcel getParcelById(Long parcelId) {
        return parcelRepository.findById(
                parcelId).orElseThrow(
                        () -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                                "No parcel found with id: " + parcelId));
    }

    /**
     * Retrieves parcels that are sent by a specified (customer)
     * 
     * @param customerId
     * @return
     */
    @Override
    public List<Parcel> getParcelsBySenderId(Long customerId) {
        Long authCustomerId = customerService.getCustomerByAuthenticatedUser().getId();
        if (authCustomerId != customerId) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
        }
        return parcelRepository.findBySenderId(customerId);
    }

    /**
     * Retrieves parcels that are recieved by a specified (customer)
     * 
     * @param customerId
     * @return
     */
    @Override
    public List<Parcel> getParcelsByRecipientId(Long customerId) {
        Long authCustomerId = customerService.getCustomerByAuthenticatedUser().getId();
        if (authCustomerId != customerId) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
        }
        return parcelRepository.findByRecipientId(customerId);
    }

    /**
     * Retrieves all parcels assigned to a specific driver.
     * 
     * @param driverId
     * @return
     */
    @Override
    public List<Parcel> getParcelsAssignedToDriver(Long driverId) {
        Long authDriverId = driverService.getAuthenticatedDriver().getId();
        if (authDriverId != driverId) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
        }

        return parcelRepository.findByDriverId(driverId);
    }

    /**
     * Retrieves a specific parcel by its id and customer (sender) id.
     * 
     * @param id
     * @param senderId
     * 
     * @return
     */
    @Override
    public Parcel getByParcelIdAndSenderId(Long id, Long senderId) {
        return parcelRepository.findByIdAndSenderId(id, senderId)
                .orElseThrow(
                        () -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));
    }

    /**
     * Retrieves a specific parcel by its id and customer (recipient) id.
     * 
     * @param id
     * @param recipientId
     * 
     * @return
     */
    @Override
    public Parcel getByParcelIdAndRecipientId(Long id, Long recipientId) {
        return parcelRepository.findByIdAndRecipientId(id, recipientId)
                .orElseThrow(
                        () -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));

    }

    /**
     * Retrieves a specific parcel by its id and customer (recipient) id.
     * 
     * @param id
     * @param recipientId
     * 
     * @return
     */
    @Override
    public Parcel getByParcelIdAndDriverId(Long id, Long driverId) {
        return parcelRepository.findByIdAndDriverId(id,
                driverId)
                .orElseThrow(
                        () -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));

    }

    /**
     * Retrieves all parcels that are currently in a specific storage.
     * 
     * @param storageId
     * @return List of parcels
     */
    @Override
    public List<Parcel> getAllParcelsInStorage(Long storageId) {
        return parcelRepository.findByStorageId(storageId);
    }

    /**
     * Retrieves intra-city parcels that are currently in a specific storage.
     * 
     * @param storageId
     * @return List of parcels
     */
    @Override
    public List<Parcel> getIntraCityParcelsInStorage(Long storageId) {
        return parcelRepository.findByStorageIdAndParcelType(storageId, ParcelType.INTRA_CITY);
    }

    /**
     * Retrieves inter-city parcels that are currently in a specific storage.
     * 
     * @param storageId
     * @return List of parcels
     */
    @Override
    public List<Parcel> getInterCityParcelsInStorage(Long storageId) {
        return parcelRepository.findByStorageIdAndParcelType(storageId, ParcelType.INTER_CITY);
    }

    /**
     * This endpoint retrieves INTRA parcels that have not been assigned to any
     * intra driver yet.
     * 
     * @param page the page number to retrieve (zero-based index)
     * @param size the size of the page to retrieve
     * @return a paginated list of unassigned inter parcels
     */
    public List<Parcel> getUnassignedIntraParcels(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcelPage = parcelRepository.findByStatus(ParcelStatus.AWAITING_INTRA_CITY_PICKUP, pageable);
        return parcelPage.getContent();
    }

    /**
     * This endpoint retrieves INTER parcels that have not been assigned to any
     * inter driver yet.
     * 
     * @param page the page number to retrieve (zero-based index)
     * @param size the size of the page to retrieve
     * @return a paginated list of unassigned intra parcels
     */
    public List<Parcel> getUnassignedInterParcels(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcelPage = parcelRepository.findByStatus(ParcelStatus.AWAITING_INTER_CITY_PICKUP, pageable);
        return parcelPage.getContent();
    }

    /**
     * Retrieves INTRA parcels that that are assigned to a specified intra driver
     * inter driver yet.
     * 
     * @param driverId
     * @return
     */
    @Override
    public List<Parcel> getParcelsAssignedToIntraCityDriver(Long driverId) {
        Driver driver = driverService.getAuthenticatedDriver();

        // Ensure the driver is an intra-city driver
        if (driver.getDriverType() != DriverType.INTRA_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver is not an intra-city driver.");
        }

        return parcelRepository.findByDriverIdAndParcelType(driverId, ParcelType.INTRA_CITY);
    }

    /**
     * Retrieves INTER parcels that that are assigned to a specified inter driver
     * inter driver yet.
     * 
     * @param driverId
     * @return
     */
    @Override
    public List<Parcel> getParcelsAssignedToInterCityDriver(Long driverId) {
        Driver driver = driverService.getAuthenticatedDriver();

        // Ensure the driver is an inter-city driver
        if (driver.getDriverType() != DriverType.INTER_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver is not an inter-city driver.");
        }

        return parcelRepository.findByDriverIdAndParcelType(driverId, ParcelType.INTER_CITY);
    }

    /**
     * finds how many parcels a driver is assigned
     * 
     * @param driver
     * 
     * @return
     */
    @Override
    public Long countParcelsByDriver(Driver driver) {
        return parcelRepository.countByDriver(driver);
    }

    /**
     * finds how many inter or intra parcels we have in the system
     * 
     * @param status
     * 
     * @return
     */
    @Override
    public Long countParcelsByStatus(ParcelStatus status) {
        return parcelRepository.countByStatus(status);
    }

    /**
     * Retrieves all unassigned parcels both ( inter and intra parcels)
     * 
     * @param page
     * @param size
     * 
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // why? to ensure that the transaction commits after
                                                           // processing each page. the status changes
    // public List<Parcel> findParcelsForDriverAssignment(int page, int size) {
    // Pageable pageable = PageRequest.of(page, size);
    // // Fetch parcels that are either awaiting intra-city or inter-city pickup
    // Page<Parcel> parcelPage = parcelRepository.findByStatusIn(
    // List.of(ParcelStatus.AWAITING_INTRA_CITY_PICKUP,
    // ParcelStatus.AWAITING_INTER_CITY_PICKUP),
    // pageable);
    // return parcelPage.getContent();
    // }

    public List<Parcel> findParcelsForDriverAssignment(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Fetch parcels that are awaiting various stages of intra-city or inter-city
        // pickup
        Page<Parcel> parcelPage = parcelRepository.findByStatusIn(
                List.of(
                        ParcelStatus.AWAITING_INTRA_CITY_PICKUP, // For intra-city deliveries
                        ParcelStatus.AWAITING_DEPARTURE_STORAGE_PICKUP, // For inter-city parcels waiting for pickup to
                                                                        // storage by intra-city delivers
                        ParcelStatus.AWAITING_INTER_CITY_PICKUP, // For inter-city parcels waiting for pickup by
                                                                 // inter-city drivers
                        ParcelStatus.AWAITING_FINAL_DELIVERY // New status for inter parcels ready for final delivery in
                                                             // destination city by intra-city drivers
                ), pageable);

        return parcelPage.getContent();
    }

    /**
     * Retrieves parcels that are currently in storage and are ready for a return
     * trip.
     *
     * @param city     The city where the storage is located.
     * @param pageable Pagination information for limiting the number of parcels
     *                 returned.
     * @return A list of parcels ready for a return trip.
     */
    @Override
    public List<Parcel> getParcelsForReturnTrip(String city, Pageable pageable) {
        return parcelRepository.findParcelsByCityAndStatus(city, ParcelStatus.AWAITING_INTER_CITY_PICKUP, pageable);

    }

    /**
     * saves a parcel
     * 
     * @param parcel
     * 
     * @return
     */
    @Override
    public void save(Parcel parcel) {
        parcelRepository.save(parcel);
    }

    @Override
    @Transactional
    public Parcel pickUpParcelFromLocker(Long parcelId, Integer transactionCode) {
        // Step 1: Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Step 2: Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Step 3: Validate the transaction code matches the parcel's transaction code
        if (!parcel.getSenderTransactionCode().equals(transactionCode)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Invalid transaction code for this parcel.");
        }

        // Step 4: Ensure the transaction code is active
        if (!parcel.getSenderTransactionCodeActive()) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Transaction code is inactive");
        }

        // Step 5: Ensure the transaction code has not expired
        if (parcel.getSenderTransactionCodeValidUntil().isBefore(LocalDateTime.now())) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Transaction code has expired");
        }

        // Step 6: Ensure the parcel is assigned to the authenticated driver
        if (parcel.getDriver() == null || !parcel.getDriver().equals(driver)) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "Parcel is not assigned to this driver");
        }

        // Step 7: Ensure the parcel is in a cabinet and and assigned to driver
        if (!parcel.getStatus().equals(ParcelStatus.ASSIGNED_TO_INTRA_CITY_DRIVER)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Parcel is not available for pickup");
        }

        // Step 8: Update parcel status based on its type (intra-city or inter-city)
        if (parcel.getParcelType() == ParcelType.INTER_CITY) {
            parcel.setStatus(ParcelStatus.IN_TRANSIT_TO_DEPARTURE_STORAGE);
        } else {
            parcel.setStatus(ParcelStatus.IN_TRANSIT_TO_RECIPIENT);
        }

        // Step 9: Mark the original cabinet as free and clear it from the parcel
        Cabinet cabinet = parcel.getCabinet();
        if (cabinet != null) {
            cabinet.setStatus(CabinetStatus.FREE);
            parcel.setCabinet(null); // Clear the cabinet from the parcel
            cabinetService.save(cabinet); // Save the cabinet status
        }

        // Step 10: Deactivate the transaction code
        parcel.setSenderTransactionCodeActive(false);

        // Step 11: If the parcel is for delivery to a recipient locker and is
        // intra-city, generate the
        // recipient's transaction code and associate the held cabinet
        if (parcel.getDeliverToRecipientLocker() && parcel.getParcelType() == ParcelType.INTRA_CITY) {
            Integer recipientTransactionCode = transactionCodeGenerator.generateTransactionCode();
            parcel.setRecipientTransactionCode(recipientTransactionCode);
            parcel.setRecipientTransactionCodeActive(true);
            parcel.setRecipientTransactionCodeValidUntil(LocalDateTime.now().plusDays(5)); // Valid for 5 days

            // Associate the held cabinet with the parcel
            Cabinet heldCabinet = cabinetService.associateHeldCabinetWithParcel(parcel,
                    parcel.getSelectedLockerLocation().getId());
            parcel.setCabinet(heldCabinet);
        }

        // Step 12: Save the updated parcel with the new cabinet (if applicable)
        return parcelRepository.save(parcel);
    }

    /**
     * Handles the storage of an inter-city parcel in the departure storage.
     * This occurs when the parcel is being sent from one city to another and needs
     * to be stored in the sender's city.
     * 
     * @param parcelId The ID of the parcel to be processed.
     * @return The updated Parcel after processing.
     */
    @Override
    public Parcel deliverToDepartureStorage(Long parcelId) {
        // Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Ensure the driver is an intra-city driver
        if (driver.getDriverType() != DriverType.INTRA_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver is not an intra-city driver.");
        }

        // Ensure the parcel has been picked up from the locker
        if (!parcel.getStatus().equals(ParcelStatus.IN_TRANSIT_TO_DEPARTURE_STORAGE)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Parcel has not been picked up from locker");
        }

        try {
            // Store the parcel in departure storage (sender's city)
            Storage storage = storageService.findOrCreateStorageForCity(parcel.getSender().getUser().getCity());
            parcel.setStorage(storage);
            // parcel.setStatus(ParcelStatus.DELIVERED_TO_DEPARTURE_STORAGE);
            parcel.setStatus(ParcelStatus.AWAITING_INTER_CITY_PICKUP);

            return parcelRepository.save(parcel);

        } catch (Exception e) {
            // Handle any exceptions that occur during the delivery process
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deliver parcel to departure storage: " + e.getMessage());
        }
    }

    /**
     * Handles delivery of an inter-city parcel to the destination storage.
     * The parcel is stored in a storage facility in the destination city for
     * further processing.
     * 
     * @param parcelId The ID of the parcel to be processed.
     * @return The updated Parcel after processing.
     */
    @Override
    public Parcel deliverToDestinationStorage(Long parcelId) {

        // Step 1: Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Step 2: Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Step 3: Ensure the driver is an inter-city driver
        if (driver.getDriverType() != DriverType.INTER_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver is not an inter-city driver.");
        }

        // Step 4: Ensure the parcel has been collected from the departure storage
        if (!parcel.getStatus().equals(ParcelStatus.IN_TRANSIT_TO_DESTINATION_STORAGE)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Parcel has not been collected yet from the destination storage");
        }

        try {
            // Step 5: Find or create the storage in the destination city
            String recipientCity = parcel.getRecipient().getUser().getCity();
            Storage storage = storageService.findOrCreateStorageForCity(recipientCity);

            // Step 6: Associate the parcel with the storage and update its status
            parcel.setStorage(storage);
            // parcel.setStatus(ParcelStatus.AWAITING_INTRA_CITY_PICKUP);
            parcel.setStatus(ParcelStatus.AWAITING_FINAL_DELIVERY); // New status indicating it's ready for final
                                                                    // delivery

            // Step 7: For inter-city parcels, associate the held cabinet and generate the
            // recipient's transaction code
            if (parcel.getDeliverToRecipientLocker()) {
                Integer recipientTransactionCode = transactionCodeGenerator.generateTransactionCode();
                parcel.setRecipientTransactionCode(recipientTransactionCode);
                parcel.setRecipientTransactionCodeActive(true);
                parcel.setRecipientTransactionCodeValidUntil(LocalDateTime.now().plusDays(5)); // Valid for 5 days

                Cabinet heldCabinet = cabinetService.associateHeldCabinetWithParcel(parcel,
                        parcel.getSelectedLockerLocation().getId());
                parcel.setCabinet(heldCabinet);
            }

            return parcelRepository.save(parcel);

        } catch (Exception e) {
            // Handle any exceptions that occur during the delivery process
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deliver parcel to destination storage: " + e.getMessage());
        }
    }

    /**
     * Handles direct delivery of a parcel to the recipient.
     * Updates the parcel status to 'DELIVERED_TO_RECIPIENT' and sends a
     * notification to the recipient.
     * 
     * @param parcelId The ID of the parcel to be delivered.
     * @return The updated Parcel after delivery.
     */
    @Override
    @Transactional
    public Parcel deliverToRecipient(Long parcelId) {

        // Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Ensure the driver is an intra-city driver
        if (driver.getDriverType() != DriverType.INTRA_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Driver is not an intra-city driver.");
        }

        // Ensure the parcel has been picked up from the locker
        if (!parcel.getStatus().equals(ParcelStatus.IN_TRANSIT_TO_RECIPIENT)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Parcel has not been picked up yet from the locker");
        }

        try {
            // Update the parcel status to indicate it has been delivered to the recipient
            parcel.setStatus(ParcelStatus.DELIVERED_TO_RECIPIENT);
            parcelRepository.save(parcel);

            // Notify the recipient of the delivery
            notificationService.sendInAppNotification(
                    parcel,
                    NotificationType.PARCEL_DELIVERED,
                    "Your parcel has been delivered",
                    parcel.getRecipient().getUser());

            return parcel;
        } catch (Exception e) {
            // Handle any exceptions that occur during the direct delivery process
            throw new TendrilExExceptionHandler(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deliver parcel to recipient: " + e.getMessage());
        }
    }

    /**
     * This method happens when a sender drops off a parcel (wether it is intra or
     * inter) into a cabinet (from the selected closest parcel locker) after the
     * parcel is created
     * 
     * @param parcelId
     * @param transactionCode
     * @return
     */
    @Override
    @Transactional
    public Parcel dropOffParcelInCabinet(Long parcelId, Integer transactionCode) {
        // Authenticate the sender (customer)
        Customer sender = customerService.getCustomerByAuthenticatedUser();

        // Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Ensure the parcel belongs to the authenticated sender
        if (!parcel.getSender().equals(sender)) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to drop off this parcel.");
        }

        // Validate the transaction code matches the parcel's transaction code
        if (!parcel.getSenderTransactionCode().equals(transactionCode)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Invalid transaction code for this parcel.");
        }

        // Ensure the transaction code is active
        if (!parcel.getSenderTransactionCodeActive()) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Transaction code is inactive");
        }

        // Ensure the transaction code has not expired
        if (parcel.getSenderTransactionCodeValidUntil().isBefore(LocalDateTime.now())) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Transaction code has expired");
        }

        // Mark the parcel as awaiting the appropriate driver assignment based on its
        // type
        if (parcel.getParcelType() == ParcelType.INTRA_CITY) {
            parcel.setStatus(ParcelStatus.AWAITING_INTRA_CITY_PICKUP);
        } else if (parcel.getParcelType() == ParcelType.INTER_CITY) {
            // parcel.setStatus(ParcelStatus.AWAITING_INTRA_CITY_PICKUP); // Intra-city
            // driver will first take it to
            // departure storage
            parcel.setStatus(ParcelStatus.AWAITING_DEPARTURE_STORAGE_PICKUP);

        }
        // Save the updated parcel
        return parcelRepository.save(parcel);
    }

    /**
     * This method happens when a driver delivers a parcel (wether it is intra or
     * inter) into a cabinet (from the selected closest parcel locker to the
     * recipent)
     * It happens in case of a pickup point delivery chosen by the sender
     * 
     * @param parcelId
     * @param recipientTransactionCode
     * @return
     */
    @Override
    @Transactional
    public Parcel deliverToRecipientPickupPoint(Long parcelId, Integer recipientTransactionCode) {
        // Step 1: Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Step 2: Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Step 3: Validate the recipient's transaction code matches the parcel's
        // transaction code
        if (!parcel.getRecipientTransactionCode().equals(recipientTransactionCode)) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Invalid recipient transaction code for this parcel.");
        }

        // Step 4: Ensure the recipient's transaction code is active
        if (!parcel.getRecipientTransactionCodeActive()) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Recipient transaction code is inactive");
        }

        // Step 5: Ensure the recipient's transaction code has not expired
        if (parcel.getRecipientTransactionCodeValidUntil().isBefore(LocalDateTime.now())) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Recipient transaction code has expired");
        }

        // Step 6: Ensure the parcel is assigned to the authenticated driver
        if (parcel.getDriver() == null || !parcel.getDriver().equals(driver)) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "Parcel is not assigned to this driver");
        }

        // Step 7: Ensure the parcel is either IN_TRANSIT_TO_RECIPIENT in case of an
        // intra parcel or AWAITING_INTRA_CITY_PICKUP in case of inter parcel point
        if (!(parcel.getStatus().equals(ParcelStatus.IN_TRANSIT_TO_RECIPIENT) ||
                parcel.getStatus().equals(ParcelStatus.AWAITING_INTRA_CITY_PICKUP))) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Parcel is not available for delivery to the recipient's pickup point");
        }

        // Proceed with delivery to the recipient's pickup point

        // Step 8: Update the parcel status to indicate it has been delivered to the
        // recipient's locker
        parcel.setStatus(ParcelStatus.DELIVERED_TO_RECIPIENT_LOCKER);

        // Step 9: Mark the recipient's transaction code as inactive after delivery
        parcel.setRecipientTransactionCodeActive(false);

        // Step 10: Save the updated parcel status and details
        return parcelRepository.save(parcel);
    }

    /**
     * Handles the pickup of an inter-city parcel from either the departure storage
     * or destination storage
     * by the appropriate driver (inter-city or intra-city).
     *
     * @param parcelId The ID of the parcel to be picked up.
     * @return The updated Parcel entity after it has been picked up.
     */
    @Override
    @Transactional
    public Parcel pickUpParcelFromStorage(Long parcelId) {
        // Step 1: Authenticate the driver
        Driver driver = driverService.getAuthenticatedDriver();

        // Step 2: Retrieve the parcel using the provided parcelId
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND,
                        "Parcel not found with id: " + parcelId));

        // Step 3: Ensure the parcel is assigned to the authenticated driver
        if (parcel.getDriver() == null || !parcel.getDriver().equals(driver)) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "Parcel is not assigned to this driver.");
        }

        // Step 4: Ensure the parcel is an inter-city parcel
        if (parcel.getParcelType() != ParcelType.INTER_CITY) {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST,
                    "Only inter-city parcels can be picked up from storage.");
        }

        // Step 5: Determine the current status and update it accordingly
        if (parcel.getStatus().equals(ParcelStatus.AWAITING_INTER_CITY_PICKUP)) {
            // The parcel is at the departure storage, ready to be picked up by an
            // inter-city driver
            parcel.setStatus(ParcelStatus.IN_TRANSIT_TO_DESTINATION_STORAGE);

        } else if (parcel.getStatus().equals(ParcelStatus.AWAITING_FINAL_DELIVERY)) {
            // The parcel has arrived at the destination storage and is ready for final
            // delivery
            parcel.setStatus(ParcelStatus.IN_TRANSIT_TO_RECIPIENT);
        } else {
            throw new TendrilExExceptionHandler(HttpStatus.BAD_REQUEST, "Parcel is not in a valid state for pickup.");
        }

        // Step 6: Clear the storage reference from the parcel after pickup
        parcel.setStorage(null);

        // Step 7: Save the updated parcel and return it
        return parcelRepository.save(parcel);
    }

}

/*
 * 
 * I have an issue! I designed the logic for batch assignment such that both
 * inter-parcels and intra-parcels within the city are initially assigned to
 * intra-city drivers where intra-city parcels are delivered directly to their
 * recipients within the city, while inter-city parcels are first sent to the
 * city’s departure storage. From there, they are handed over to an inter-city
 * driver who transports them to the destination city’s storage facility. Once
 * there, an intra-city driver in the destination city is assigned to deliver
 * the parcel to the recipient’s home address or pickup point, if selected.
 * 
 * so the issue lies somewhere in using the parcel type
 * I explicity specified that all parcels statuses are updated
 * to AWAITING_INTRA_CITY_PICKUP after they are left at the cabinets during
 * dropOffParcelInCabinet by the
 * sender
 * 
 * Status vs. Type
 */

/*
 * 
 * THE WORKFLOW
 * 
 */