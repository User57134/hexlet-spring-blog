package io.hexlet.spring.repository;

import io.hexlet.spring.model.Post;
import io.hexlet.spring.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
