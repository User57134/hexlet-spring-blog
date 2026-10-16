package io.hexlet.spring.mapper;

import io.hexlet.spring.model.BaseEntity;
import jakarta.persistence.EntityManager;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ReferenceMapper {
    @Autowired private EntityManager entityManager;

    /**
     * Метод берет на вход объект типа сущности и находит в базе по идентификатору в соответствующей указанному типу
     * таблице нужную запись, создает на ее основе сущность и возвращает.
     *
     * @param id - идентификатор записи
     * @param entityClass - объект типа сущности
     * @return объект сущности.
     * @param <T> - тип сущности
     */
    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return (id != null) ? entityManager.find(entityClass, id) : null;
    }
}
