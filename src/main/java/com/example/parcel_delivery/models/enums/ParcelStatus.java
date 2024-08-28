package com.example.parcel_delivery.models.enums;


public enum ParcelStatus {
    CREATED,  
    AWAITING_DRIVER_ASSIGNMENT, // when parcel is in database and has not been assigned yet
    AWAITING_PICKUP, // when parcel is in the cabinet and is waiting for a driver to pick it up
    IN_TRANSIT_TO_DESTINATION_STORAGE, // when inter driver takes the parcel and delivers it to storage
    IN_TRANSIT_TO_DEPARTURE_STORAGE, // when driver takes the parcel and delivers it to storage
    IN_TRANSIT_TO_RECIPIENT,  //when parcel is taken from a cabinet and being delivered to the in-city recipient OR when parcel is delivered from a storage to a in-city recipient ( in a case of a parcel coming from a different city)                
    DELIVERED_TO_RECIPIENT,   //when parcel is delivered to final-end destination ( recipient)
    DELIVERED_TO_DEPARTURE_STORAGE,  // when parcel is delivered to storage by intra driver
    DELIVERED_TO_DESTINATION_STORAGE  // when parcel is delivered to storage by inter driver

}
 