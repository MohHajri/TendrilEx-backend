package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.requests.RegisterReqDTO;
import com.example.parcel_delivery.models.dtos.responses.UserResDTO;
import com.example.parcel_delivery.models.entities.User;

// @Mapper(componentModel = "spring")
// public interface UserMapper {

//     UserResDTO toUserResDTO(User user);

//     @Mapping(target = "customer", ignore = true)
//     @Mapping(target = "driver", ignore = true)
//     @Mapping(target = "id", ignore = true)
//     @Mapping(target = "roles", ignore = true)
//     User toUserEntity(RegisterReqDTO registerReqDTO);
  

// }


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "firstname", source = "firstName")
    @Mapping(target = "lastname", source = "lastName")
    UserResDTO toUserResDTO(User user);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "userPoint", ignore = true)
    @Mapping(target = "firstName", source = "firstname")
    @Mapping(target = "lastName", source = "lastname")
    User toUserEntity(RegisterReqDTO registerReqDTO);
}
