package io.hexlet.spring.controller.api;

import com.jayway.jsonpath.JsonPath;
import io.hexlet.spring.data.ModelGenerator;
import io.hexlet.spring.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
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
class PostControllerTest {

    @Value("${app.page-size}")
    private int maxPageSize;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository repository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    @Test
    // Returns status 200 and a page of posts
    void indexOfPublishedTest() throws Exception {
        for (int i = 0; i < maxPageSize; i++) {
            var post = modelGenerator.makePost(null);
            post.setPublished(true);
            repository.save(post);
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

        var result = mockMvc.perform(get("/api/old/posts"))
                .andExpect(status().isOk()).andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).isArray().hasSize(maxPageSize);
    }


    @Test
        // Returns status 200 and an array of posts
    void showTest() throws Exception {
        var post = repository.save(modelGenerator.makePost(null));

        var content = mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(post.getId()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(post.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(post.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(post.isPublished())
        );
    }


    @Test
        // Returns status 201 and the Post
    void createTest() throws Exception {
        var post = modelGenerator.makePost(null);

        var result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isPresent(),
                jsonAssert -> jsonAssert.node("title").isEqualTo(post.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(post.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(post.isPublished())
        );

        Long generatedId = JsonPath.parse(content).read("$.id", Long.class);

        assertThat(repository.findById(generatedId).isPresent()).isTrue();
    }


    @Test
        // Returns status 200 and the updated Post
    void updateTest() throws Exception {
        var post = repository.save(modelGenerator.makePost(null));;

        var newPostData = modelGenerator.makePost(null);

        var content = mockMvc.perform(put("/api/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newPostData)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(post.getId()),
                jsonAssert -> jsonAssert.node("title").isEqualTo(newPostData.getTitle()),
                jsonAssert -> jsonAssert.node("content").isEqualTo(newPostData.getContent()),
                jsonAssert -> jsonAssert.node("published").isEqualTo(newPostData.isPublished())
        );
    }


    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var post = repository.save(modelGenerator.makePost(null));

        mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isNoContent());
    }
}
