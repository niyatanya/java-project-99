package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.instancio.Select;
import net.datafaker.Faker;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private ObjectMapper om;

    private User testUser;

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker();
        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .create();
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);

        MvcResult result = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId()).with(jwt());

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName())
        );
    }

    @Test
    public void testCreate() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail(testUser.getEmail());
        dto.setPassword(testUser.getPassword());
        dto.setFirstName(testUser.getFirstName());
        dto.setLastName(testUser.getLastName());

        MockHttpServletRequestBuilder request = post("/api/users")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        User user = userRepository.findByFirstNameAndLastName(
                testUser.getFirstName(), testUser.getLastName()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
    }

    @Test
    public void testCreateWithNotValidEmail() throws Exception {
        UserDTO dto = mapper.map(testUser);
        dto.setEmail("notvalidemail.");

        MockHttpServletRequestBuilder request = post("/api/users")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        userRepository.save(testUser);

        UserDTO dto = mapper.map(testUser);
        dto.setFirstName("New name");
        dto.setLastName("New last name");

        MockHttpServletRequestBuilder request = put("/api/users/{id}", testUser.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        User user = userRepository.findById(testUser.getId()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(user.getLastName()).isEqualTo(dto.getLastName());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        userRepository.save(testUser);

        Map<String, String> dto = new HashMap<>();
        dto.put("firstName", "Another name");

        MockHttpServletRequestBuilder request = put("/api/users/{id}", testUser.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        User user = userRepository.findById(testUser.getId()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(dto.get("firstName"));
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    public void testDelete() throws Exception {
        userRepository.save(testUser);

        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(false);
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        userRepository.save(testUser);

        MockHttpServletRequestBuilder request = get("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
