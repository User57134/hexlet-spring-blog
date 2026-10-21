package io.hexlet.spring.controller.api;


import io.hexlet.spring.component.ModelGenerator;
import io.hexlet.spring.dto.*;
import io.hexlet.spring.repository.PostRepository;
import io.hexlet.spring.repository.TagRepository;
import io.hexlet.spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Полностью отключает Spring Security фильтры для этого теста
@ActiveProfiles("test")
@Transactional
public class TagControllerTest {
    @Value("${app.page-size}")
    private int maxPageSize;

    private final MockMvc mockMvc;
    private final TagRepository tagRepository;
    private final ModelGenerator modelGenerator;
    private final ObjectMapper objectMapper;

    @Autowired
    public TagControllerTest(MockMvc mock,
                              TagRepository repositoryOfTags,
                              PostRepository repositoryOfPosts,
                              UserRepository repositoryOfUsers,
                              ModelGenerator generator,
                              ObjectMapper mapper) {

        mockMvc = mock;
        tagRepository = repositoryOfTags;
        modelGenerator = generator;
        objectMapper = mapper;
    }


    @Test
        // Returns status 200 and an array of tags
    void indexTest() throws Exception {
        for (int i = 0; i < maxPageSize; i += 1) {
           var tag = modelGenerator.makeTag(null);
           tagRepository.save(tag);
        }

        var result = mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk()).andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).isArray().hasSize(maxPageSize);
    }


    @Test
        // Returns status 200 and an array of posts
    void showTest() throws Exception {
        var tag = tagRepository.save(modelGenerator.makeTag(null));

        var content = mockMvc.perform(get("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(tag.getId()),
                jsonAssert -> jsonAssert.node("name").isEqualTo(tag.getName())
        );
    }


    @Test
        // Returns status 201 and the Post
    void createTest() throws Exception {
        var tag = modelGenerator.makeTag(null);

        var tagCreateDto = new TagCreateDTO();
        tagCreateDto.setName(tag.getName());

        var result = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagCreateDto)))
                .andExpect(status().isCreated())
                .andReturn();

        var content = result.getResponse().getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isPresent(),
                jsonAssert -> jsonAssert.node("name").isEqualTo(tag.getName())
        );

        assertThat(tagRepository.findByName(tag.getName())).isPresent();
    }


    @Test
        // Returns status 200 and the updated Post
    void updateTest() throws Exception {
        var tag = tagRepository.save(modelGenerator.makeTag(null));

        var tagUpdateDTO = new TagUpdateDTO();
        final String NEW_TAG_NAME = "new_test_tag";
        tagUpdateDTO.setName(NEW_TAG_NAME);

        var content = mockMvc.perform(put("/api/tags/{id}", tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(tag.getId()),
                jsonAssert -> jsonAssert.node("name").isEqualTo(NEW_TAG_NAME)

        );

        assertThat(tagRepository.findByName(NEW_TAG_NAME)).isPresent();
    }


    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var tag = tagRepository.save(modelGenerator.makeTag(null));

        mockMvc.perform(delete("/api/tags/{id}", tag.getId()))
                .andExpect(status().isNoContent());
    }
}

