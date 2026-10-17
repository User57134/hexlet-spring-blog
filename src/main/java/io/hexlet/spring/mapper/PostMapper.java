package io.hexlet.spring.mapper;

import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.dto.PostUpdateDTO;
import io.hexlet.spring.dto.TagCreateDTO;
import io.hexlet.spring.model.Post;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.repository.TagRepository;
import org.mapstruct.*;


import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {
                TimestampUtil.class,
                JsonNullableMapper.class,
                ReferenceMapper.class,
                TagMapper.class // Позволяет MapStruct использовать маппинг Tag -> TagDTO для метода makeDTO
        },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PostMapper { // Переключили на abstract class

    @Autowired
    private TagRepository tagRepository; // Внедряем репозиторий для проверки уникальности тегов

    @Mapping(target = "authorId", source = "author.id")
    public abstract PostDTO makeDTO(Post post);

    // Привязываем автора по ID, а теги маппим через специальный метод
    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapStringToTags")
    public abstract Post makeEntity(PostCreateDTO dto);

    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapStringToTags")
    public abstract void updateEntity(PostUpdateDTO dto, @MappingTarget Post post);

    /**
     * Собственный метод для отображения из Set<TagCreateDTO> в Set<Tag>.
     * Он проверяет каждый тег по имени: если тег уже есть в БД, берется он (вместе со своим ID).
     * Если тега нет — создается новый объект, который сохранится каскадом.
     */
    @Named("mapStringToTags")
    protected Set<Tag> mapStringToTags(Set<TagCreateDTO> tagDtos) {
        if (tagDtos == null) {
            return new HashSet<>();
        }

        return tagDtos.stream()
                .map(dto -> tagRepository.findByName(dto.getName())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(dto.getName());
                            return newTag;
                        }))
                .collect(Collectors.toSet());
    }
}
