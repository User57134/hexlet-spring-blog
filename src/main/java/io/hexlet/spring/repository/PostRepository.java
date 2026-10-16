package io.hexlet.spring.repository;

import io.hexlet.spring.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    /* Поиск всех опубликованных постов */
    Page<Post> findByPublishedTrue(Pageable pageable);

    /* Поиск записи по слагу */
    Optional<Post> findBySlug(String slug);
}
