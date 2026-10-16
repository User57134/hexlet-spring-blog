package io.hexlet.spring.controller.api;

import com.jayway.jsonpath.JsonPath;
import io.hexlet.spring.data.ModelGenerator;
import io.hexlet.spring.dto.PostCreateDTO;
import io.hexlet.spring.dto.PostPatchDTO;
import io.hexlet.spring.dto.PostUpdateDTO;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc  // Создает бин MockMvc и кладет его в контекст
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
            var post = modelGenerator.makePost(user);
            post.setPublished(true);
            postRepository.save(post);
        }

        var result = mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "5"))
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

        assertThatJson(content).isArray().hasSize(maxPageSize);
    }


    @Test
        // Returns status 200 and an array of posts
    void showTest() throws Exception {
        var user = modelGenerator.makeUser();
        var tetsPost = modelGenerator.makePost(user);
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
                jsonAssert -> jsonAssert.node("published").isEqualTo(post.isPublished())
        );
    }


    @Test
        // Returns status 201 and the Post
    void createTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());;
        var post = modelGenerator.makePost(user);

        var postCreateDto = new PostCreateDTO();
        postCreateDto.setSlug(post.getSlug());
        postCreateDto.setTitle(post.getTitle());
        postCreateDto.setContent(post.getContent());
        postCreateDto.setAuthorId(post.getAuthor().getId());

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
                jsonAssert -> jsonAssert.node("published").isEqualTo(false)
        );

        Long generatedId = JsonPath.parse(content).read("$.id", Long.class);

        assertThat(postRepository.findById(generatedId)).isPresent();
    }


    @Test
        // Returns status 200 and the updated Post
    void updateTest() throws Exception {
        var user = modelGenerator.makeUser();
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
                jsonAssert -> jsonAssert.node("authorId").isEqualTo(postForUpdate.getAuthor().getId())
        );

        var resavedPost = postRepository.findById(post.getId());
        assertThat(resavedPost).isPresent();
        assertThat(resavedPost.get().getTitle()).isEqualTo(postForUpdate.getTitle());
    }


    @Test
        // Returns status 200 and the updated Post
    void partialUpdateTest() throws Exception {
        var user = modelGenerator.makeUser();
        var post = modelGenerator.makePost(user);
        postRepository.save(post);

        var postPatchDTO = new PostPatchDTO();
        postPatchDTO.setTitle(JsonNullable.of("NewTitle"));

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
                jsonAssert -> jsonAssert.node("authorId").isEqualTo(post.getAuthor().getId())
        );

        var resavedPost = postRepository.findById(post.getId());
        assertThat(resavedPost).isPresent();
        assertThat(resavedPost.get().getTitle()).isEqualTo("NewTitle");
    }


    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var post = postRepository.save(modelGenerator.makePost(null));

        mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isNoContent());
    }
}
