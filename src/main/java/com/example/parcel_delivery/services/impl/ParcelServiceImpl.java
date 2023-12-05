package com.example.parcel_delivery.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.parcel_delivery.models.dtos.requests.ParcelReqDTO;
import com.example.parcel_delivery.models.entities.Cabinet;
import com.example.parcel_delivery.models.entities.Customer;
import com.example.parcel_delivery.models.entities.Parcel;
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
        parcel.setIsRecipientRegistered(isRecipientRegistered);
        parcel.setTransactionCode(transactionCode);
        parcel.setTransactionCodeValidUntil(LocalDateTime.now().plusDays(12));
        parcel.setStatus(ParcelStatus.SENT);
        parcel.setCabinet(reservedCabinet);
        parcel.setSelectedLockerLocation(ParcelLockerService.getParcelLockerById(parcelReqDTO.getSelectedLockerId()));
        parcel.setCreatedAt(LocalDateTime.now());
        parcel.setIdempotencyKey(parcelReqDTO.getIdempotencyKey());
        parcel.setIdempotencyKeyCreatedAt(LocalDateTime.now());
    

        //set parcel in cabinet
        reservedCabinet.setCurrentParcel(parcel);

        // Save parcel
        Parcel savedParcel = parcelRepository.save(parcel);

        // Send notifications
        if (recipient != null) {
            // Registered recipient
            notificationService.sendInAppNotification(savedParcel);
            notificationService.sendSmsNotification(parcelReqDTO.getRecipientPhoneNo(), transactionCode);
        } else {
            // Unregistered recipient
            notificationService.sendSmsNotification(parcelReqDTO.getRecipientPhoneNo(), transactionCode);
        }

        return savedParcel;
    }
}
