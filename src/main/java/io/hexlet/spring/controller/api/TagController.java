package io.hexlet.spring.controller.api;

import io.hexlet.spring.dto.TagUpdateDTO;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.dto.TagCreateDTO;
import io.hexlet.spring.dto.TagDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.TagMapper;
import io.hexlet.spring.repository.TagRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Autowired
    public TagController(TagRepository repository, TagMapper mapper) {
        tagRepository = repository;
        tagMapper = mapper;
    }

    @GetMapping(path = "")
    @ResponseStatus(HttpStatus.OK)
    public List<TagDTO> index() {
        var tags = tagRepository.findAll();

        return tags.stream().map(tagMapper::makeDTO).toList();
    }


    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TagDTO show(@PathVariable long id) {
        var tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with the id = " + id + " not found"));

        return tagMapper.makeDTO(tag);
    }


    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public TagDTO create(@Valid @RequestBody TagCreateDTO dto) {
        var tag = tagRepository.findByName(dto.getName())
                .orElseGet(() -> {
                    var newTag = new Tag(dto.getName());
                    return tagRepository.save(newTag);
                });

        return tagMapper.makeDTO(tag);
    }


    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public TagDTO update(@PathVariable long id, @Valid @RequestBody TagUpdateDTO dto) {
        var tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with the id = " + id + " not found"));

        if (!tag.getName().equals(dto.getName())) {
            if (tagRepository.findByName(dto.getName()).isEmpty()) {
              tagMapper.updateEntity(dto, tag);
              tagRepository.save(tag);
            } else {
                throw new IllegalArgumentException("Tag with the name '" + dto.getName() + "' already exists");
            }
        }

        return tagMapper.makeDTO(tag);
    }


    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        tagRepository.deleteById(id);
    }
}
