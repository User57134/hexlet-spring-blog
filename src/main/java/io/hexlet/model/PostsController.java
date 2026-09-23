package io.hexlet.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PostsController {
    // Хранилище добавленных страниц, то есть обычный список
    private List<Post> posts = new ArrayList<Post>();

    // Создание поста
    @PostMapping("/posts")
    public ResponseEntity<Post> create(@RequestBody Post post) {
        posts.add(post);

        var createdPost = posts.getLast();
        try {
            var createdUri = new URI("/posts/" + createdPost.getSlug());
            return ResponseEntity.created(createdUri).body(createdPost);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Список постов
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var result = posts.stream().limit(limit).toList();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }

    @GetMapping("/posts/{id}") // Вывод страницы
    public ResponseEntity<Post> show(@PathVariable String id) {
        var post = posts.stream()
                .filter(p -> p.getSlug().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return ResponseEntity.ok().body(post);
    }

    @PutMapping("/posts/{id}") // Обновление страницы
    public Post update(@PathVariable String id, @RequestBody Post data) {
        var post =
                posts.stream()
                        .filter(p -> p.getSlug().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var title = data.getTitle();
        if (title != null && !title.isEmpty()) {
            post.setTitle(title);
        }

        var content = data.getContent();
        if (content != null && !content.isEmpty()) {
            post.setContent(content);
        }

        post.setAuthor(data.getAuthor());
        post.setCreatedAt(data.getCreatedAt());

        return post;
    }

    @DeleteMapping("/posts/{id}") // Удаление страницы
    public ResponseEntity<Void> destroy(@PathVariable String id) {
        if (posts.removeIf(p -> p.getSlug().equals(id))) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
