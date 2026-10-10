package io.hexlet.spring.controller.api;

import com.jayway.jsonpath.JsonPath;
import io.hexlet.spring.data.ModelGenerator;
import io.hexlet.spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc  // Создает бин MockMvc и кладет его в контекст
@Transactional         // После прогона каждого теста, база возвращается в исходное состояние
class UserControllerTest {

    @Value("${app.page-size}")
    private int maxPageSize;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Test
        // Returns status 201 and Body
    void createTest() throws Exception {
        var body =
                """
                    {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john@example.com"
                    }
                """;

        var result = mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andReturn();

        var content = result.getResponse().getContentAsString();

        Long generatedId = JsonPath.parse(content).read("$.id", Long.class);

        assertThat(repository.findById(generatedId).isPresent()).isTrue();
    }


    @Test
        // Returns 200 and List<User>
    void indexTest() throws Exception {
        modelGenerator.generateData(maxPageSize);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(maxPageSize));
    }

    @Test
        // Returns 200 and User
    void showTest() throws Exception {
        var user = repository.save(modelGenerator.makeUser());

        var content = mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var context = JsonPath.parse(content);

        assertThat(context.read("$.id", Long.class)).isEqualTo(user.getId());
        assertThat(context.read("$.firstName", String.class)).isEqualTo(user.getFirstName());
        assertThat(context.read("$.lastName", String.class)).isEqualTo(user.getLastName());
        assertThat(context.read("$.email", String.class)).isEqualTo(user.getEmail());

        String birthdayStr = context.read("$.birthday", String.class);
        assertThat(Instant.parse(birthdayStr)).isEqualTo(user.getBirthday());
    }

    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var user = repository.save(modelGenerator.makeUser());

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }
}