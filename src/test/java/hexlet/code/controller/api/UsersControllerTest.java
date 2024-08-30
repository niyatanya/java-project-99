package hexlet.code.controller.api;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.UserService;
import hexlet.code.util.InstanceGenerator;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.format.DateTimeFormatter;
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
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskRepository taskRepository;

    private JwtRequestPostProcessor token;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private ObjectMapper om;

    private User testUser;

    private final InstanceGenerator generator = new InstanceGenerator();

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        statusRepository.deleteAll();
        testUser = generator.getUser();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
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
        int index = testUser.getCreatedAt().toString().indexOf(".");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testUser.getId()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("createdAt").asString().contains(testUser.getCreatedAt()
                        .format(DateTimeFormatter.ISO_DATE_TIME).substring(0, index))
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

        User user = userRepository.findByEmail(testUser.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.getPasswordDigest()).isNotEqualTo(testUser.getPassword());
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
                .with(token)
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
                .with(token)
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

        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(false);
    }

    @Test
    public void testDeleteUserWithTask() throws Exception {
        userRepository.save(testUser);

        TaskStatus testStatus = generator.getTaskStatus();
        statusRepository.save(testStatus);

        Task testTask = generator.getTask();
        testTask.setTaskStatus(testStatus);
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isLocked());

        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(true);
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        userRepository.save(testUser);
        User testUser2 = generator.getUser();
        userRepository.save(testUser2);

        MockHttpServletRequestBuilder request = delete("/api/users/{id}", testUser2.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(userRepository.existsById(testUser.getId())).isEqualTo(true);
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        userRepository.save(testUser);

        UserDTO dto = mapper.map(testUser);
        dto.setFirstName("New name (must not be saved)");

        MockHttpServletRequestBuilder request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        User user = userRepository.findById(testUser.getId()).get();

        assertThat(user.getFirstName()).isNotEqualTo(dto.getFirstName());
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
    }
}
