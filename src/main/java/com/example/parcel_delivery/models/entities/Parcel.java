package com.example.parcel_delivery.models.entities;

import java.time.LocalDateTime;

import com.example.parcel_delivery.models.enums.ParcelStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parcels")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double depth;

    @Column(nullable = false)
    private Double mass;

    @Column(nullable = false)
    private String description;

    @Column
    private String unregisteredRecipientName;

    @Column
    private String unregisteredRecipientPhone;

    @Column
    private String unregisteredRecipientAddress;

    @Column
    private Boolean isRecipientRegistered = false;

    @Column(nullable = false, unique = true)
    private Integer transactionCode;

    @Column(nullable = false)
    private Boolean transactionCodeActive = true;

    @Column(nullable = false)
    private LocalDateTime transactionCodeValidUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcelStatus status;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @JsonBackReference
    private Customer sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Customer recipient; 

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cabinet_id", referencedColumnName = "id")
    private Cabinet cabinet;

    @ManyToOne
    @JoinColumn(name = "storage_id", referencedColumnName = "id")
    private Storage storage;

    @ManyToOne
    @JoinColumn(name = "selected_locker_location_id")
    private ParcelLocker selectedLockerLocation;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private LocalDateTime statusUpdatedAt;

    @Column
    private String idempotencyKey;

    @Column
    private LocalDateTime idempotencyKeyCreatedAt;


}
