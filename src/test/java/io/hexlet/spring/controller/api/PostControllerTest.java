package io.hexlet.spring.controller.api;

import com.jayway.jsonpath.JsonPath;
import io.hexlet.spring.component.ModelGenerator;
import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostPatchDTO;
import io.hexlet.spring.dto.PostUpdateDTO;
import io.hexlet.spring.dto.TagCreateDTO;
import io.hexlet.spring.model.Tag;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.TagRepository;
import io.hexlet.spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.util.HashSet;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Полностью отключает Spring Security фильтры для этого теста
@ActiveProfiles("test")
//@AutoConfigureMockMvc  // Создает бин MockMvc и кладет его в контекст
@Transactional         // После прогона каждого теста, база возвращается в исходное состояние
public class PostControllerTest {

    @Value("${app.page-size}")
    private int maxPageSize;

    private final MockMvc mockMvc;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper objectMapper;

    @Autowired
    public PostControllerTest(MockMvc mock,
                              PostRepository repositoryOfPosts,
                              UserRepository repositoryOfUsers,
                              ModelGenerator generator,
                              ObjectMapper mapper) {

        mockMvc = mock;
        postRepository = repositoryOfPosts;
        userRepository = repositoryOfUsers;
        modelGenerator = generator;
        objectMapper = mapper;
    }

    @Test
    // Returns status 200 and a page of posts
    void indexOfPublishedTest() throws Exception {
        for (int i = 0; i < maxPageSize; i++) {
            var user = modelGenerator.makeUser();
            userRepository.save(user);

            var post = modelGenerator.makePost(user);
            post.setPublished(true);
            postRepository.save(post);
        }

        var result = mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", String.valueOf(maxPageSize)))
                .andExpect(status().isOk()).andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).node("content").isArray().hasSize(maxPageSize);
    }


    @Test
        // Returns status 200 and an array of posts
    void indexTest() throws Exception {
        modelGenerator.generateData(maxPageSize);

        var result = mockMvc.perform(get("/api/posts/old"))
                .andExpect(status().isOk()).andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).and(
                // Проверяем, что сам корневой JSON является массивом и имеет размер maxPageSize
                jsonAssert -> jsonAssert.isArray().hasSize(maxPageSize)
        );

        @SuppressWarnings("unchecked")
        List<String> allTagNames = JsonPath.parse(content).read("$.[*].tags[*].name", List.class);

        // 3. Через стандартный AssertJ проверяем, что общее количество связей Many-to-Many равно 2 * maxPageSize
        assertThat(allTagNames)
                .isNotNull()
                .hasSize(2 * maxPageSize);
    }


    @Test
        // Returns status 200 and and an array of posts
    void indexWithFilterTest() throws Exception {
        var pascalTag = new Tag("pascal");
        var cppTag = new Tag("cpp");
        var javaTag = new Tag("java");
        var phpTag = new Tag("php");
        var jsTag = new Tag("js");
        var goTag = new Tag("go");

        // 1ый пользователь написал посты о pascal и c++
        var user = userRepository.save(modelGenerator.makeUser());
        var post1 = modelGenerator.makePost(user);
        post1.addTag(pascalTag);
        postRepository.save(post1);

        var post2 = modelGenerator.makePost(user);
        post2.addTag(cppTag);
        postRepository.save(post2);

        // 2ой пользователь написал посты о c++ и java
        user = userRepository.save(modelGenerator.makeUser());
        var post3 = modelGenerator.makePost(user);
        post3.addTag(cppTag);
        postRepository.save(post3);

        var post4 = modelGenerator.makePost(user);
        post4.addTag(javaTag);
        postRepository.save(post4);

        // 3ий пользователь написал посты о java и php
        user = userRepository.save(modelGenerator.makeUser());
        var post5 = modelGenerator.makePost(user);
        post5.addTag(javaTag);
        postRepository.save(post5);

        var post6 = modelGenerator.makePost(user);
        post6.addTag(phpTag);
        postRepository.save(post6);

        // 4ый пользователь написал пост о php и js
        user = userRepository.save(modelGenerator.makeUser());
        var post7 = modelGenerator.makePost(user);
        post7.addTag(phpTag);
        post7.addTag(jsTag);
        postRepository.save(post7);

        // 5ый пользователь написал посты о java, cpp и go
        user = userRepository.save(modelGenerator.makeUser());
        var post8 = modelGenerator.makePost(user);
        post8.setTitle("Develop with the Spring framework");
        post8.addTag(javaTag);
        postRepository.save(post8);

        var post9 = modelGenerator.makePost(user);
        post9.addTag(cppTag);
        post9.addTag(goTag);
        postRepository.save(post9);

        //****** 1ый запрос GET /api/posts?tagId=1
        var result = mockMvc.perform(
                get("/api/posts/filter").param("tagId", pascalTag.getId().toString())
        ).andExpect(status().isOk()).andReturn();

        var content = result.getResponse().getContentAsString();

        // Должен вернуться ровно 1 пост (пост 1-го юзера про pascal)
        assertThatJson(content)
                .isArray()
                .hasSize(1);

        // Заходим в массив тегов первого найденного поста, и убеждаемся что там 'pascal'
        assertThatJson(content)
                .node("[0].tags")
                .isArray()
                .element(0).node("name").isEqualTo("pascal");

        //****** 2ой запрос GET /api/posts?tagsIn=1,2
        result = mockMvc.perform(
                get("/api/posts/filter")
                        .param(
                                "tagsIn",
                                pascalTag.getId().toString(),
                                cppTag.getId().toString())
        ).andExpect(status().isOk()).andReturn();

        content = result.getResponse().getContentAsString();

        // 1. Должны вернуться 4 поста (1ый, 2ой, 3ий,9ый)
        List<Integer> actualIds = JsonPath.read(content, "$.[*].id");

        // 2. Убеждаемся, что вернулись имеено посты 1, 2, 3, 9
        assertThat(actualIds).containsExactlyInAnyOrder(
                post1.getId().intValue(),
                post2.getId().intValue(),
                post3.getId().intValue(),
                post9.getId().intValue()
        );

        // 3. Убеждаемся, что в общем массиве тегов только теги 'pascal' и 'cpp'
        List<String> tagNames = JsonPath.read(content, "$.[*].tags[*].name");
        assertThat(tagNames).contains("pascal", "cpp");

        /* Не работает, т.к библиотека JsonUnit не поддерживает сложный синтаксис JSONPath вроде $[*.id]
           или $[*].id внутри метода .node()пути JsonPath в элементе node: .node("$[*].id") */
        /*
        assertThatJson(content)
                .node("$[*].id")
                .isArray()
                .containsExactlyInAnyOrder(
                        post1.getId(),
                        post2.getId(),
                        post3.getId(),
                        post9.getId()
                );

        // 2. Проверяем теги (вложенный массив tags[*].name соберется в один плоский массив строк)
        assertThatJson(content)
                .node("$[*].tags[*].name")
                .isArray()
                .contains("pascal", "cpp");

           Но есть возможность сравнения с образцом!!!

           Пример:
           assertThatJson(content)
               .when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER)
               .isEqualTo(""" [
                   {"id": %d, "tags": [{"name": "pascal"}]},
                   {"id": %d, "tags": [{"name": "cpp"}]},
                   {"id": %d, "tags": [{"name": "cpp"}]},
                   {"id": %d, "tags": [{"name": "go"}, {"name": "cpp"}]}
               ]
               """.formatted(post1.getId(), post2.getId(), post3.getId(), post9.getId()));
        */

        //****** 3ий запрос GET /api/posts?tagId=3&authorId=5&nameCont=spring
        result = mockMvc.perform(
                get("/api/posts/filter")
                        .param("tagId", javaTag.getId().toString())
                        .param("authorId", user.getId().toString())
                        .param("nameCont", "spring")
        ).andExpect(status().isOk()).andReturn();

        content = result.getResponse().getContentAsString();

        // 1. Должны вернуться 9 пост
        actualIds = JsonPath.read(content, "$.[*].id");

        // 2. Убеждаемся, что вернулись имеено посты 1, 2, 3, 9
        assertThat(actualIds).hasSize(1);
        assertThat(actualIds.getFirst()).isEqualTo(post8.getId().intValue());
    }

    @Test
        // Returns status 200 and an array of posts
    void showTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());
        var tetsPost = modelGenerator.makePost(user);
        modelGenerator.makeTag(tetsPost);
        modelGenerator.makeTag(tetsPost);

        var post = postRepository.save(tetsPost);

        var content = mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(post.getId()),
                jsonAssert -> jsonAssert.node("slug").isEqualTo(post.getSlug()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(post.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(post.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(post.isPublished()),
                jsonAssert -> jsonAssert.node("tags").isArray().hasSize(post.getTags().size())
        );
    }


    @Test
        // Returns status 201 and the Post
    void createTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());
        var post = modelGenerator.makePost(user);

        var postCreateDto = new PostCreateDTO();
        postCreateDto.setSlug(post.getSlug());
        postCreateDto.setTitle(post.getTitle());
        postCreateDto.setContent(post.getContent());
        postCreateDto.setAuthorId(post.getAuthor().getId());

        var tags = new HashSet<TagCreateDTO>();
        for (int i = 0; i < 4; i += 1) {
            var dto = new TagCreateDTO();
            dto.setName("test_tag_" + i);
            tags.add(dto);
        }

        postCreateDto.setTags(tags);

        var result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreateDto)))
                .andExpect(status().isCreated())
                .andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isPresent(),
                jsonAssert -> jsonAssert.node("slug").isEqualTo(post.getSlug()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(post.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(post.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(false),
                jsonAssert -> jsonAssert.node("tags").isArray().hasSize(tags.size())
        );

        Long generatedId = JsonPath.parse(content).read("$.id", Long.class);

        var savedPostOptional = postRepository.findById(generatedId);
        assertThat(savedPostOptional).isPresent();
        
        var savedPost = savedPostOptional.get();
        var savedTagNames = savedPost.getTags().stream().map(Tag::getName).toList();
        
        assertThat(savedTagNames)
                .containsExactlyInAnyOrder("test_tag_0", "test_tag_1", "test_tag_2", "test_tag_3");
    }


    @Test
        // Returns status 200 and the updated Post
    void updateTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());
        var post = modelGenerator.makePost(user);
        postRepository.save(post);

        var userForUpdate = userRepository.save(modelGenerator.makeUser());
        var postForUpdate = modelGenerator.makePost(userForUpdate);

        var postUpdateDTO = new PostUpdateDTO();
        postUpdateDTO.setSlug(postForUpdate.getSlug());
        postUpdateDTO.setTitle(postForUpdate.getTitle());
        postUpdateDTO.setContent(postForUpdate.getContent());
        postUpdateDTO.setPublished(postForUpdate.isPublished());
        postUpdateDTO.setAuthorId(postForUpdate.getAuthor().getId());

        var tags = new HashSet<TagCreateDTO>();
        for (int i = 0; i < 4; i += 1) {
            var dto = new TagCreateDTO();
            dto.setName("test_tag_" + i);
            tags.add(dto);
        }

        postUpdateDTO.setTags(tags);

        var content = mockMvc.perform(put("/api/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(post.getId()),
                jsonAssert -> jsonAssert.node("slug").isEqualTo(postForUpdate.getSlug()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(postForUpdate.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(postForUpdate.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(postForUpdate.isPublished()),
                jsonAssert -> jsonAssert.node("authorId").isEqualTo(postForUpdate.getAuthor().getId()),
                jsonAssert -> jsonAssert.node("tags").isArray().hasSize(tags.size())
        );

        var savedPostOptional = postRepository.findById(post.getId());
        assertThat(savedPostOptional).isPresent();

        var savedPost = savedPostOptional.get();
        assertThat(savedPost.getTitle()).isEqualTo(postForUpdate.getTitle());

        var savedTagNames = savedPost.getTags().stream().map(Tag::getName).toList();
        assertThat(savedTagNames)
                .containsExactlyInAnyOrder("test_tag_0", "test_tag_1", "test_tag_2", "test_tag_3");
    }


    @Test
        // Returns status 200 and the updated Post
    void partialUpdateTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());
        var post = modelGenerator.makePost(user);
        postRepository.save(post);

        var postPatchDTO = new PostPatchDTO();
        postPatchDTO.setTitle(JsonNullable.of("NewTitle"));

        var tags = new HashSet<TagCreateDTO>();
        for (int i = 0; i < 4; i += 1) {
            var dto = new TagCreateDTO();
            dto.setName("test_tag_" + i);
            tags.add(dto);
        }

        postPatchDTO.setTags(JsonNullable.of(tags));

        var content = mockMvc.perform(patch("/api/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postPatchDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(post.getId()),
                jsonAssert -> jsonAssert.node("slug").isEqualTo(post.getSlug()),
                jsonAssert -> jsonAssert.node("title").isEqualTo("NewTitle"),
                jsonAssert -> jsonAssert.node("content").isEqualTo(post.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(post.isPublished()),
                jsonAssert -> jsonAssert.node("authorId").isEqualTo(post.getAuthor().getId()),
                jsonAssert -> jsonAssert.node("tags").isArray().hasSize(tags.size())
        );

        var savedPostOptional = postRepository.findById(post.getId());
        assertThat(savedPostOptional).isPresent();

        var savedPost = savedPostOptional.get();
        assertThat(savedPost.getTitle()).isEqualTo(postPatchDTO.getTitle().get());

        var savedTagNames = savedPost.getTags().stream().map(Tag::getName).toList();
        assertThat(savedTagNames)
                .containsExactlyInAnyOrder("test_tag_0", "test_tag_1", "test_tag_2", "test_tag_3");
    }


    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var post = postRepository.save(modelGenerator.makePost(null));

        mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isNoContent());
    }
}
