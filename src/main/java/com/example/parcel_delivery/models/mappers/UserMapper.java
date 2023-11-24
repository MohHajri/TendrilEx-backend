package com.example.parcel_delivery.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.parcel_delivery.models.dtos.requests.RegisterReqDTO;
import com.example.parcel_delivery.models.dtos.responses.UserResDTO;
import com.example.parcel_delivery.models.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResDTO toUserResDTO(User user);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUserEntity(RegisterReqDTO registerReqDTO);
  

}
