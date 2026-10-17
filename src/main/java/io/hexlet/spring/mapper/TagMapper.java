package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.TagCreateDTO;
import io.hexlet.spring.dto.TagDTO;
import io.hexlet.spring.dto.TagUpdateDTO;
import io.hexlet.spring.model.Tag;
import org.mapstruct.*;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagDTO makeDTO(Tag tag);

    Tag makeEntity(TagCreateDTO dto);

    void updateEntity(TagUpdateDTO dto, @MappingTarget Tag tag);
}

