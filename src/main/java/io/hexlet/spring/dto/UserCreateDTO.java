package io.hexlet.spring.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;

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
