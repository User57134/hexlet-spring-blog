package io.hexlet.spring.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@NoArgsConstructor
@Setter
@Getter
public class PostPatchDTO {
    @Size(min=3, max=100, message = "Заглавие должно быть от 3 до 100 символов длинной")
    private JsonNullable<String> title = JsonNullable.undefined();

    @Size(min=10, message = "Содержание должно быть от 10 символов длинной")
    private JsonNullable<String> content = JsonNullable.undefined();

    private JsonNullable<Boolean> published = JsonNullable.undefined();
}
