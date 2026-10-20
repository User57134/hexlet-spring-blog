package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.model.User;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(
        uses = {TimestampUtil.class, JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract UserDTO makeDTO(User user);

    @Mapping(target = "passwordHash", source = "password", qualifiedByName = "encryptPassword")
    public abstract User makeEntity(UserCreateDTO dto);

    @Mapping(target = "passwordHash", source = "password")
    public abstract void updateEntity(@MappingTarget User user, UserUpdateDTO dto);

    @Mapping(target = "passwordHash", source = "password", qualifiedByName = "encryptPassword")
    public abstract void updateEntity(@MappingTarget User user, UserPatchDTO dto);

    /**
     * Собственный метод для реализации автоматического шифрования пароля.
     * Вызывается при преобразовании из CreateDTO, UpdateDTO в сущность.
     * Внутри сущности пароль хранится в виде хэша.
     */
    @Named("encryptPassword")
    protected String encryptPassword(String password) {
        if (password == null || password.isBlank()) {
            return null;
        }

        return passwordEncoder.encode(password);
    }
}
