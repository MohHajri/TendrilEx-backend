package com.example.parcel_delivery.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Parcel;
import com.example.parcel_delivery.models.enums.NotificationType;
import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.example.parcel_delivery.repositories.ParcelRepo;
import com.example.parcel_delivery.services.CabinetService;
import com.example.parcel_delivery.services.CustomerService;
import com.example.parcel_delivery.services.NotificationService;
import com.example.parcel_delivery.services.ParcelLockerService;
import com.example.parcel_delivery.services.ParcelService;
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


    @Override
    public Parcel sendNewParcel(ParcelReqDTO parcelReqDTO) {
        //check if parcel is already sent
        Optional<Parcel> existingParcel = parcelRepository.findByIdempotencyKey(parcelReqDTO.getIdempotencyKey());
        if (existingParcel.isPresent()) {
            return existingParcel.get();
        }

        // Get auth customer and act as a sender
        Customer sender = customerService.getCustomerByAuthenticatedUser();
        if(sender == null) {
            throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
        }
        Customer recipient = customerService.findCustomerByPhoneNumber(parcelReqDTO.getRecipientPhoneNo()).orElse(null);
        boolean isRecipientRegistered = recipient != null;

        Point senderLocation = locationUtil.getLocationFromDTO(parcelReqDTO);
        sender.getUser().setUserPoint(senderLocation);

        // Select and Reserve a Cabinet
        Cabinet reservedCabinet = cabinetService.reserveCabinetFromThe5Lockers(parcelReqDTO.getSelectedLockerId());

        // Generate Transaction Code
        Integer transactionCode = transactionCodeGenerator.generateTransactionCode();

        // Create Parcel
        Parcel parcel = new Parcel();
        parcel.setSender(sender);
        parcel.setDepth(parcelReqDTO.getDepth());
        parcel.setHeight(parcelReqDTO.getHeight());
        parcel.setWidth(parcelReqDTO.getWidth());
        parcel.setMass(parcelReqDTO.getMass());
        parcel.setDescription(parcelReqDTO.getDescription());
        parcel.setUnregisteredRecipientName(parcelReqDTO.getRecipientName());
        parcel.setUnregisteredRecipientPhone(parcelReqDTO.getRecipientPhoneNo());
        parcel.setUnregisteredRecipientAddress(parcelReqDTO.getRecipientAddress());
        parcel.setUnregisteredRecipientEmail(parcelReqDTO.getRecipientEmail());
        parcel.setUnregisteredRecipientPostcode(parcelReqDTO.getRecipientPostcode());
        parcel.setUnregisteredRecipientCity(parcelReqDTO.getRecipientCity());
        parcel.setIsRecipientRegistered(isRecipientRegistered);
        parcel.setTransactionCode(transactionCode);
        parcel.setTransactionCodeValidUntil(LocalDateTime.now().plusDays(12));
        parcel.setStatus(ParcelStatus.AWAITING_DRIVER_ASSIGNMENT);
        parcel.setCabinet(reservedCabinet);
        parcel.setSelectedLockerLocation(ParcelLockerService.getParcelLockerById(parcelReqDTO.getSelectedLockerId()));
        parcel.setCreatedAt(LocalDateTime.now());
        parcel.setIdempotencyKey(parcelReqDTO.getIdempotencyKey());
        parcel.setIdempotencyKeyCreatedAt(LocalDateTime.now());
    
        //save recipient if registered
        if (recipient != null) {
            parcel.setRecipient(recipient);
        }

        //set parcel in cabinet
        reservedCabinet.setCurrentParcel(parcel);

        // Save parcel
        Parcel savedParcel = parcelRepository.save(parcel);

        // Send notifications
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
    }

    @Override
    public Parcel getParcelById(Long id) {
        return parcelRepository.
                findById(id).
                orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));
          }

    @Override
    public List<Parcel> getSentParcelsByCustomerId(Long id) {
            Long authCustomerId = customerService.getCustomerByAuthenticatedUser().getId();
            if (authCustomerId != id) {
                throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
            } 
            return parcelRepository.findBySenderId(id);
    }

    @Override
    public List<Parcel> getReceivedParcelsByCustomerId(Long id) {
            Long authCustomerId = customerService.getCustomerByAuthenticatedUser().getId();
            if (authCustomerId != id) {
                throw new TendrilExExceptionHandler(HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
            } 
            return parcelRepository.findByRecipientId(id);
        }

    @Override
    public Parcel getByParcelIdAndSenderId(Long id, Long senderId) {
        return parcelRepository.
                findByIdAndSenderId(id, senderId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));
       }

    @Override
    public Parcel getByParcelIdAndRecipientId(Long id, Long recipientId) {
        return parcelRepository.
                findByIdAndRecipientId(id, recipientId)
                .orElseThrow(() -> new TendrilExExceptionHandler(HttpStatus.NOT_FOUND, "No parcel found with id: " + id));

        }

    @Override
    public Parcel driverPicksUp(Long parcelId) {
      return null; }

    @Override
    public Parcel driverDelivers(Long parcelId) {
        return null;
    }

    @Override
    public List<Parcel> findParcelsForDriverAssignment(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Parcel> parcelPage = parcelRepository.findByStatus(ParcelStatus.AWAITING_DRIVER_ASSIGNMENT, pageable);
        return parcelPage.getContent();
    }

    @Override
    public void save(Parcel parcel) {
        parcelRepository.save(parcel);
        }

    }
