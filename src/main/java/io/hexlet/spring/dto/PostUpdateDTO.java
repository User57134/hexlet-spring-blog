package io.hexlet.spring.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PostUpdateDTO {
    @NotBlank(message = "Заглавие не должно быть пустым")
    @Size(min=3, max=100, message = "Заглавие должно быть от 3 до 100 символов длинной")
    private String title;

    @NotBlank(message = "Содержание не должно быть пустым")
    @Size(min=10, message = "Содержание должно быть от 10 символов длинной")
    private String content;

    private boolean published;
}
