package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.UserCreateDTO;
import io.hexlet.spring.dto.UserDTO;
import io.hexlet.spring.dto.UserUpdateDTO;
import io.hexlet.spring.model.User;
import org.mapstruct.*;


@Mapper(
        uses = TimestampUtil.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDTO makeDTO(User user);

    User makeEntity(UserCreateDTO dto);

    void updateEntity(@MappingTarget User user, UserUpdateDTO dto);
}
