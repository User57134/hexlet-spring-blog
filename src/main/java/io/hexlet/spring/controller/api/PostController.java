package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PostMapper;
import io.hexlet.spring.model.Post;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.UserRepository;
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
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Autowired
    public PostController(PostRepository repositoryOfPosts,
                          PostMapper mapper,
                          UserRepository repositoryOfUsers) {

        postRepository = repositoryOfPosts;
        postMapper = mapper;
        userRepository = repositoryOfUsers;
    }


    @GetMapping("/old")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PostDTO>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var result = postRepository.findAll();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result.stream().map(postMapper::makeDTO).toList());
    }


    @GetMapping(path = "")
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

       return postRepository.findByPublishedTrue(pageable).map(postMapper::makeDTO);
    }


    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO show(@PathVariable Long id) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));

        return postMapper.makeDTO(post);
    }


    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        var user = userRepository.findById(dto.getUserId())
                .orElseThrow(()
                        -> new ResourceNotFoundException("User with the id =" + dto.getUserId() + " not found"));


        var entity = postMapper.makeEntity(dto);
        entity.setAuthor(user);

        var post = postRepository.save(entity);

        // 2. Динамически строим URI созданного ресурса: текущий путь + /{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // берет текущий "/posts"
                .path("/{id}")        // добавляет переменную пути
                .buildAndExpand(post.getId()) // подставляет id нового поста
                .toUri();

        return ResponseEntity.created(location).body(postMapper.makeDTO(post));
    }


    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO data) {
        var post = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with the id = " + id + " not found"));

        postMapper.updateEntity(post, data);

        return postMapper.makeDTO(postRepository.save(post));
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDTO partialUpdate(@PathVariable Long id, @RequestBody PostPatchDTO dto) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));

        dto.getTitle().ifPresent(post::setTitle);
        dto.getContent().ifPresent(post::setContent);
        dto.getPublished().ifPresent(post::setPublished);

        return postMapper.makeDTO(postRepository.save(post));
    }


    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Удаление страницы
    public void destroy(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}
