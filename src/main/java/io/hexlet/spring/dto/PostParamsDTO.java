package io.hexlet.spring.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class PostParamsDTO {
    private String nameCont;
    private Long authorId;
    private LocalDate createdAtGt;
    private LocalDate createdAtLt;
    private Long tagId;
    private List<Long> tagsIn;
}
