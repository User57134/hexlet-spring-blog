package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.converter.DtoEntityConverter;
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

    private final PostRepository postRepository;
    private final DtoEntityConverter dtoEntityConvertor;

    @Autowired
    public PostController(PostRepository repository, DtoEntityConverter converter) {
        postRepository = repository;
        dtoEntityConvertor = converter;
    }

    // Создание поста
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        Post post = postRepository.save(dtoEntityConvertor.makeEntity(dto));

        // 2. Динамически строим URI созданного ресурса: текущий путь + /{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // берет текущий "/posts"
                .path("/{id}")        // добавляет переменную пути
                .buildAndExpand(post.getId()) // подставляет id нового поста
                .toUri();

        return ResponseEntity.created(location).body(dtoEntityConvertor.makeDTO(post));
    }

    // Список постов
    @GetMapping("/old/posts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDTO>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var result = postRepository.findAll();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result.stream().map(dtoEntityConvertor::makeDTO).toList());
    }

    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public Page<PostDTO> indexOfPublished(
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

       return postRepository.findByPublishedTrue(pageable).map(dtoEntityConvertor::makeDTO);
    }

    @GetMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    // Вывод страницы
    public PostDTO show(@PathVariable Long id) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));

        return dtoEntityConvertor.makeDTO(post);
    }

    @PutMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    // Обновление страницы
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody Post data) {
        var post = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with the id = " + id + " not found"));

        post.setTitle(data.getTitle());
        post.setContent(data.getContent());
        post.setPublished(data.isPublished());

        return dtoEntityConvertor.makeDTO(postRepository.save(post));
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Удаление страницы
    public void destroy(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}
