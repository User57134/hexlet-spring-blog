package io.hexlet.spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PostCreateDTO {
    @NotBlank(message = "Заглавие не должно быть пустым")
    @Size(min=3, max=100, message = "Заглавие должно быть от 3 до 100 символов длинной")
    private String title;

    @NotBlank(message = "Содержание не должно быть пустым")
    @Size(min=10, message = "Содержание должно быть от 10 символов длинной")
    private String content;

    @NotNull(message = "ID пользователя обязателен для заполнения")
    @Positive(message = "ID пользователя - положительное число")
    private Long userId;
}
