package com.example.parcel_delivery.models.entities;

import java.time.LocalDateTime;

import com.example.parcel_delivery.models.enums.ParcelStatus;

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

    @Column(nullable = false, unique = true)
    private String transactionCode;

    @Column(nullable = false)
    private Boolean transactionCodeActive = true;


    @Column(nullable = false)
    private LocalDateTime transactionCodeValidUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcelStatus status;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @OneToOne
    @JoinColumn(name = "cabinet_id", referencedColumnName = "id")
    private Cabinet cabinet;

    @ManyToOne
    @JoinColumn(name = "storage_id", referencedColumnName = "id")
    private Storage storage;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime statusUpdatedAt;

}
