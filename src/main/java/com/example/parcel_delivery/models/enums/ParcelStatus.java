package com.example.parcel_delivery.models.enums;

public enum ParcelStatus {
    CREATED, // when parcel is just created in the system by sender and has not been drop off
             // to a cabinet yet.

    AWAITING_INTRA_CITY_PICKUP, // after a parcel is dropped off to a cabinet, it will wait for an intra driver
                                // to take it ( to reciepent or ot a departure storage)

    AWAITING_INTER_CITY_PICKUP, // after an inter parcel is delivered to a departure storage, it will wait for
                                // for a pickup by an inter-city driver to the destination storage

    ASSIGNED_TO_INTER_CITY_DRIVER,

    ASSIGNED_TO_INTRA_CITY_DRIVER,

    IN_TRANSIT_TO_DESTINATION_STORAGE, // when inter driver takes the parcel and delivers it to storage

    IN_TRANSIT_TO_DEPARTURE_STORAGE, // when driver takes the parcel and delivers it to storage

    IN_TRANSIT_TO_RECIPIENT, // when parcel is taken from a cabinet and being delivered to the in-city
                             // recipient OR when parcel is delivered from a storage to a in-city recipient (
                             // in a case of a parcel coming from a different city)

    DELIVERED_TO_RECIPIENT, // when parcel is delivered to final-end destination (
                            // recipient home address)

    DELIVERED_TO_RECIPIENT_LOCKER, // when parcel is delivered to final-end destination ( recipient's chosen
                                   // closest pickup point)

    AWAITING_DEPARTURE_STORAGE_PICKUP,

    AWAITING_FINAL_DELIVERY,

}

// AWAITING_DEPARTURE_STORAGE_PICKUP
// AWAITING_FINAL_DELIVERY