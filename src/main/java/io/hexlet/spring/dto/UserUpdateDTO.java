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
public class UserUpdateDTO {
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min=3, message = "Имя должно содержать хотябы 3 символа")
    private String firstName;

    @NotBlank(message = "Фамилия не должна быть пустой")
    @Size(min=2, message = "Фамилия должна содержать хотябы 2 символа")
    private String lastName;

    @NotBlank(message = "Почта не должно быть пустым")
    private String email;

    private LocalDate birthday;
}
