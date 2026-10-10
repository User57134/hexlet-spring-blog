package io.hexlet.spring.controller.api;

import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.model.Post;
import io.hexlet.spring.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired private PostRepository repository;

    // Создание поста
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Post> create(@Valid @RequestBody Post post) {
        Post savedPost = repository.save(post);

        // 2. Динамически строим URI созданного ресурса: текущий путь + /{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // берет текущий "/posts"
                .path("/{id}")        // добавляет переменную пути
                .buildAndExpand(savedPost.getId()) // подставляет id нового поста
                .toUri();

        return ResponseEntity.created(location).body(savedPost);
    }

    // Список постов
    @GetMapping("/old/posts")
    public ResponseEntity<List<Post>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var result = repository.findAll();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }

    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public Page<Post> indexOfPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = null;

        if ((sort != null) && (!sort.isBlank())) {
            var sortParameters = sort.split(",");

            var sortField = sortParameters[0].trim();
            var sortOrder = Sort.Order.asc(sortField);

            if (sortParameters.length == 2) {
                if (sortParameters[1].trim().equalsIgnoreCase("desc")) {
                    sortOrder = Sort.Order.desc(sortField);
                }
            }

            pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        } else {
            pageable = PageRequest.of(page, size);
        }

       return repository.findByPublishedTrue(pageable);
    }

    @GetMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    // Вывод страницы
    public Post show(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));
    }

    @PutMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    // Обновление страницы
    public Post update(@PathVariable Long id, @Valid @RequestBody Post data) {
        var post = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with the id = " + id + " not found"));

        post.setTitle(data.getTitle());
        post.setContent(data.getContent());
        post.setPublished(data.isPublished());
        post.setAuthorId(data.getAuthorId());
        post.setCreatedAt(data.getCreatedAt());

        return repository.save(post);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Удаление страницы
    public void destroy(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
