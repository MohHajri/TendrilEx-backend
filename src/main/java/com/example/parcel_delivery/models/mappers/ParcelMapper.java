package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.locationtech.jts.geom.Point;
import com.example.parcel_delivery.models.dtos.responses.ParcelResDTO;
import com.example.parcel_delivery.models.entities.Parcel;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(source = "id", target = "parcelId")
    @Mapping(source = "transactionCodeValidUntil", target = "codeExpiryDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "selectedLockerLocation.lockerPoint", target = "parcelLockerLocationPoint", qualifiedByName = "pointToString")
    @Mapping(source = "sender.user.firstname", target = "senderName")
    @Mapping(source = "parcel", target = "recipientName", qualifiedByName = "getRecipientName")
    ParcelResDTO toParcelResDTO(Parcel parcel);

    @Named("pointToString")
    default String pointToString(Point point) {
        if (point != null) {
            double longitude = point.getX();
            double latitude = point.getY();
            return latitude + ", " + longitude;
        }
        return null;
    }

    @Named("getRecipientName")
    default String getRecipientName(Parcel parcel) {
        if (parcel.getIsRecipientRegistered() != null && parcel.getIsRecipientRegistered()) {
            return parcel.getRecipient() != null ? parcel.getRecipient().getUser().getFirstname() : null;
        } else {
            return parcel.getUnregisteredRecipientName();
        }
    }
}
