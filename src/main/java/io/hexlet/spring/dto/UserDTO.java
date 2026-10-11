package io.hexlet.spring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
