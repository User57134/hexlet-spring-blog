package io.hexlet.spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;


@NoArgsConstructor
@Getter
@Setter
public class UserPatchDTO {
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private JsonNullable<String> firstName = JsonNullable.undefined();

    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private JsonNullable<String> lastName = JsonNullable.undefined();

    @Email
    private JsonNullable<String> email = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<String> password = JsonNullable.undefined();

    @Past
    private JsonNullable<LocalDate> birthday = JsonNullable.undefined();
}