package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.responses.GeoPointDTO;
import com.example.parcel_delivery.models.dtos.responses.ParcelLockerResDTO;
import com.example.parcel_delivery.models.entities.ParcelLocker;
import org.locationtech.jts.geom.Point;

@Mapper(componentModel = "spring")
public interface ParcelLockerMapper {

    @Mapping(target = "lockerId", source = "id")
    @Mapping(target = "lockerPoint", expression = "java(convertToPointDTO(parcelLocker.getGeoLocation()))")
    ParcelLockerResDTO toParcelLockerResDTO(ParcelLocker parcelLocker);

    default GeoPointDTO convertToPointDTO(Point point) {
        if (point == null) {
            return null;
        }
        return new GeoPointDTO(point.getY(), point.getX());
    }
}
