package io.hexlet.spring.service;

import io.hexlet.spring.dto.*;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.PostMapper;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.TagRepository;
import io.hexlet.spring.specification.PostSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashSet;
import java.util.List;

@Service
public class PostService {
    @Value("${app.page-size}")
    private int maxPageSize;

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final PostSpecification postSpecificationBuilder;

    @Autowired
    public PostService(PostRepository repositoryOfPosts,
                          TagRepository repositoryOfTags,
                          PostMapper mapper,
                          PostSpecification specification) {

        postRepository = repositoryOfPosts;
        tagRepository = repositoryOfTags;
        postMapper = mapper;
        postSpecificationBuilder = specification;
    }


    public List<PostDTO> findAll(int page, int size) {
        var result = postRepository.findAll();

        return result.stream()
                .map(postMapper::makeDTO)
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();
    }


    public List<PostDTO> findAllBy(PostParamsDTO params, int page, int size) {
        int pageSize = Math.min(size, maxPageSize);

        var spec = postSpecificationBuilder.build(params);
        var pgPosts = postRepository.findAll(spec, PageRequest.of(page - 1, pageSize));
        var pgPostsDTO = pgPosts.map(postMapper::makeDTO);

        return pgPostsDTO.toList();
    }


    public Page<PostDTO> findAllPublished(int page, int size, String sort) {
        int pageSize = Math.min(size, maxPageSize);
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

            pageable = PageRequest.of(page, pageSize, Sort.by(sortOrder));

        } else {
            pageable = PageRequest.of(page, pageSize);
        }

        return postRepository.findByPublishedTrue(pageable).map(postMapper::makeDTO);
    }


    public PostDTO findById(Long id) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with the id =" + id + " not found"));

        return postMapper.makeDTO(post);
    }


    @Transactional
    public PostDTO create(PostCreateDTO dto) {
        var post = postRepository.save(postMapper.makeEntity(dto));

        return postMapper.makeDTO(post);
    }


    @Transactional
    public PostDTO update(Long id, PostUpdateDTO data) {
        var post = postRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with the id = " + id + " not found"));

        postMapper.updateEntity(data, post);

        return postMapper.makeDTO(postRepository.save(post));
    }


    @Transactional
    public PostDTO partialUpdate(Long id, PostPatchDTO dto) {
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


    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
}
