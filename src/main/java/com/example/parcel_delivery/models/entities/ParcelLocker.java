package com.example.parcel_delivery.models.entities;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name = "parcel_lockers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ParcelLocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "location_point", columnDefinition = "geometry(Point,4326)")
    private Point lockerPoint;

    @OneToMany(mappedBy = "lockerLocation")
    @JsonManagedReference
    private Set<Cabinet> cabinets;
    
}