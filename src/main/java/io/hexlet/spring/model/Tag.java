package io.hexlet.spring.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Tag implements BaseEntity{
    private  Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;


}
