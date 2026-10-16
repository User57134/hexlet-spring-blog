package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.dto.PostUpdateDTO;
import io.hexlet.spring.model.Post;
import org.mapstruct.*;


@Mapper(
        uses = {
                TimestampUtil.class,      // Используется для преобразования Instant в LocalDateTime

                JsonNullableMapper.class, // Используется для преобразования работы с JsonNullable значениями

                ReferenceMapper.class     // Используется  для автоматического ленивой или прямой загрузки сущностей
                                          // при маппинге из DTO.
        },

        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    @Mapping(target = "authorId", source = "author.id")
    PostDTO makeDTO(Post post);

    @Mapping(target = "author", source = "authorId")
    Post makeEntity(PostCreateDTO dto);

    @Mapping(target = "author", source = "authorId")
    void updateEntity(PostUpdateDTO dto, @MappingTarget Post post);
}
