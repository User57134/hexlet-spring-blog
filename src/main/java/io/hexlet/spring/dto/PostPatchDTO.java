package io.hexlet.spring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
public class PostPatchDTO {
    private JsonNullable<String> slug = JsonNullable.undefined();

    @Size(min=3, max=100, message = "Заглавие должно быть от 3 до 100 символов длинной")
    private JsonNullable<String> title = JsonNullable.undefined();

    @Size(min=10, message = "Содержание должно быть от 10 символов длинной")
    private JsonNullable<String> content = JsonNullable.undefined();

    private JsonNullable<Boolean> published = JsonNullable.undefined();

    @Valid // для проверки, что теги не пустые
    private JsonNullable<Set<TagCreateDTO>> tags = JsonNullable.undefined();

    @Positive(message = "ID пользователя - положительное число")
    private JsonNullable<Long> authorId = JsonNullable.undefined();
}
