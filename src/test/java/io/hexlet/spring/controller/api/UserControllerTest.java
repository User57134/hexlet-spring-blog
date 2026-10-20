package io.hexlet.spring.controller.api;

import com.jayway.jsonpath.JsonPath;
import io.hexlet.spring.component.ModelGenerator;
import io.hexlet.spring.dto.UserPatchDTO;
import io.hexlet.spring.dto.UserUpdateDTO;
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
import java.time.LocalDate;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Полностью отключает Spring Security фильтры для этого теста
//@AutoConfigureMockMvc  // Создает бин MockMvc и кладет его в контекст
@Transactional         // После прогона каждого теста, база возвращается в исходное состояние
public class UserControllerTest {

    @Value("${app.page-size}")
    private int maxPageSize;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
        // Returns status 201 and Body
    void createTest() throws Exception {
        var body =
                """
                    {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john@example.com",
                    "password": "1234567"
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

        assertThat(userRepository.findById(generatedId)).isPresent();
    }

    @Test
        // Returns status 200 and the updated User
    void updateTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());

        var dataForUpdate = modelGenerator.makeUser();

        var userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(dataForUpdate.getFirstName());
        userUpdateDTO.setLastName(dataForUpdate.getLastName());
        userUpdateDTO.setEmail(dataForUpdate.getEmail());
        userUpdateDTO.setBirthday(dataForUpdate.getBirthday());
        userUpdateDTO.setPassword("12345678");

        var content = mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(user.getId()),
                jsonAssert -> jsonAssert.node("firstName").isEqualTo(dataForUpdate.getFirstName()),
                jsonAssert -> jsonAssert.node("lastName").isEqualTo(dataForUpdate.getLastName()),
                jsonAssert -> jsonAssert.node("email").isEqualTo(dataForUpdate.getEmail()),
                jsonAssert -> jsonAssert.node("birthday").isEqualTo(dataForUpdate.getBirthday())
        );
    }


    @Test
        // Returns status 200 and the updated User
    void partialUpdateTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());

        var dataForUpdate = modelGenerator.makeUser();

        var userPatchDTO = new UserPatchDTO();
        userPatchDTO.setLastName(JsonNullable.of(dataForUpdate.getLastName()));
        userPatchDTO.setPassword(JsonNullable.of("12345678"));

        var content = mockMvc.perform(patch("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPatchDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(content).and(
                jsonAssert -> jsonAssert.node("id").isEqualTo(user.getId()),
                jsonAssert -> jsonAssert.node("firstName").isEqualTo(user.getFirstName()),
                jsonAssert -> jsonAssert.node("lastName").isEqualTo(dataForUpdate.getLastName()),
                jsonAssert -> jsonAssert.node("email").isEqualTo(user.getEmail()),
                jsonAssert -> jsonAssert.node("birthday").isEqualTo(user.getBirthday())
        );
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
        var user = userRepository.save(modelGenerator.makeUser());

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
        assertThat(LocalDate.parse(birthdayStr)).isEqualTo(user.getBirthday());
    }

    @Test
        // Returns status 204
    void deleteTest() throws Exception {
        var user = userRepository.save(modelGenerator.makeUser());

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }
}