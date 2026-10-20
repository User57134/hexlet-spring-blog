package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService service) {
        postService = service;
    }


    @GetMapping("/old")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDTO>> index(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        var result = postService.findAll(page, size);

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }


    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public Page<PostDTO> indexOfPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        return postService.findAllPublished(page, size, sort);
    }


    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public List<PostDTO> indexWithFilter(
            PostParamsDTO params,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return postService.findAllBy(params, page, size);
    }


    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO show(@PathVariable Long id) {
        return postService.findById(id);
    }


    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        var postDto = postService.create(dto);

        // 2. Динамически строим URI созданного ресурса: текущий путь + /{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()              // берет текущий "/posts"
                .path("/{id}")                     // добавляет переменную пути
                .buildAndExpand(postDto.getId())   // подставляет id нового поста
                .toUri();

        return ResponseEntity.created(location).body(postDto);
    }


    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO data) {
        return postService.update(id, data);
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO partialUpdate(@PathVariable Long id, @RequestBody PostPatchDTO data) {
        return postService.partialUpdate(id, data);
    }


    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Удаление страницы
    public void destroy(@PathVariable Long id) {
        postService.deleteById(id);
    }
}
