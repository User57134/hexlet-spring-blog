package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.dto.PostUpdateDTO;
import io.hexlet.spring.model.Post;
import org.mapstruct.*;


@Mapper(
        uses = TimestampUtil.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    PostDTO makeDTO(Post post);

    Post makeEntity(PostCreateDTO dto);

    void updateEntity(@MappingTarget Post post, PostUpdateDTO dto);
}
