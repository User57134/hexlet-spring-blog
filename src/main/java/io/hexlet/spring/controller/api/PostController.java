package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PostMapper;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.TagRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Value("${app.page-size}")
    private int maxPageSize;

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    @Autowired
    public PostController(PostRepository repositoryOfPosts,
                          TagRepository repositoryOfTags,
                          PostMapper mapper) {

        postRepository = repositoryOfPosts;
        tagRepository = repositoryOfTags;
        postMapper = mapper;
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

            pageable = PageRequest.of(page, Math.min(size, maxPageSize), Sort.by(sortOrder));

        } else {
            pageable = PageRequest.of(page, Math.min(size, maxPageSize));
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
    @Transactional
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostCreateDTO dto) {
        var post = postRepository.save(postMapper.makeEntity(dto));

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
    @Transactional
    public PostDTO update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO data) {
        var post = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with the id = " + id + " not found"));

        postMapper.updateEntity(data, post);

        return postMapper.makeDTO(postRepository.save(post));
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public PostDTO partialUpdate(@PathVariable Long id, @RequestBody PostPatchDTO dto) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));

        dto.getTitle().ifPresent(post::setTitle);
        dto.getContent().ifPresent(post::setContent);
        dto.getPublished().ifPresent(post::setPublished);

        dto.getTags().ifPresent(tagDtos -> {
            // Очищаем старый список тегов
            var helperSet = new HashSet<>(post.getTags());
            helperSet.forEach(post::removeTag);

            // Навешиваем новые связи
            if (tagDtos != null) {
                tagDtos.forEach(tagDto -> {
                    Tag tag = tagRepository.findByName(tagDto.getName())
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagDto.getName());
                                return newTag;
                            });
                    post.addTag(tag);
                });
            }
        });

        return postMapper.makeDTO(postRepository.save(post));
    }


    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Удаление страницы
    public void destroy(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}
