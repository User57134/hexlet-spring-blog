package io.hexlet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Post {
    private String title;
    private String content;
    private String author;
    private Instant createdAt;
    private String slug;
}