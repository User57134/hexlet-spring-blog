package io.hexlet.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PostsController {
    // Хранилище добавленных страниц, то есть обычный список
    private List<Post> posts = new ArrayList<Post>();

    // Список постов
    @GetMapping("/posts")
    public List<Post> index(@RequestParam(defaultValue = "10") Integer limit) {
        return posts.stream().limit(limit).toList();
    }

    // Создание поста
    @PostMapping("/posts")
    public Post create(@RequestBody Post post) {
        posts.add(post);
        return post;
    }

    @GetMapping("/posts/{id}") // Вывод страницы
    public Optional<Post> show(@PathVariable String id) {
        var post = posts.stream().filter(p -> p.getSlug().equals(id)).findFirst();
        return post;
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
    public void destroy(@PathVariable String id) {
        posts.removeIf(p -> p.getSlug().equals(id));
    }
}
