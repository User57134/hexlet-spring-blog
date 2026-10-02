package io.hexlet.controller.api;

import io.hexlet.exception.ResourceNotFoundException;
import io.hexlet.model.Post;
import io.hexlet.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class PostsController {

    private PostRepository repository;

    @Autowired
    public PostsController(PostRepository postRepository) {
        repository = postRepository;
    }

    // Создание поста
    @PostMapping("/posts")
    public ResponseEntity<Post> create(@Valid @RequestBody Post post) {
        var savedPost = repository.save(post);

        try {
            var createdUri = new URI("/posts/" + savedPost.getId());
            return ResponseEntity.created(createdUri).body(savedPost);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Список постов
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var result = repository.findAll();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }

    @GetMapping("/posts/{id}") // Вывод страницы
    public Post show(@PathVariable Long id) {
        var post = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return post;
    }

    @PutMapping("/posts/{id}") // Обновление страницы
    public Post update(@PathVariable Long id, @Valid @RequestBody Post data) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        var post = repository.findById(id).get();


        var title = data.getTitle();
        if (title != null && !title.isEmpty()) {
            post.setTitle(title);
        }

        var content = data.getContent();
        if (content != null && !content.isEmpty()) {
            post.setContent(content);
        }

        post.setPublished(data.isPublished());
        post.setAuthor(data.getAuthor());
        post.setCreatedAt(data.getCreatedAt());

        return repository.save(post);
    }

    @DeleteMapping("/posts/{id}") // Удаление страницы
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
