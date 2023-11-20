package com.example.parcel_delivery.models.entities;


import com.example.parcel_delivery.models.enums.CabinetStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "cabinets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Cabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CabinetStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_locker_location_id", referencedColumnName = "id")
    private ParcelLockerLocation lockerLocation;

    @OneToOne
    @JoinColumn(name = "parcel_id", referencedColumnName = "id")
    private Parcel currentParcel;


    //cabinet size and dimensions
    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double depth;

}