package io.hexlet.spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;


@NoArgsConstructor
@Getter
@Setter
public class UserCreateDTO {
    @NotBlank(message = "Поле 'Имя' не должно быть пустым")
    @Size(min=3, message = "Имя должно содержать хотябы 3 символа")
    private String firstName;

    @NotBlank(message = "Поле 'Фамилия' не должно быть пустым")
    @Size(min=2, message = "Фамилия должна содержать хотябы 2 символа")
    private String lastName;

    @NotBlank(message = "Поле 'Почта' не должно быть пустым")
    private String email;

    private LocalDate birthday;
}
