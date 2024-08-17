package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private ObjectMapper om;

    private Task testTask;

    private TaskStatus testStatus;

    private User testUser;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        Faker faker = new Faker();
        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .create();
        userRepository.save(testUser);

        testStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .create();
        statusRepository.save(testStatus);

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getTaskStatus))
                .ignore(Select.field(Task::getLabels))
                .create();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);

        MvcResult result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();

        taskRepository.delete(testTask);
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(jwt());

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName())
        );

        taskRepository.delete(testTask);
    }

    @Test
    public void testCreate() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setIndex(123);
        dto.setAssigneeId(testUser.getId());
        dto.setTitle("Add front to app");
        dto.setContent("Install npm, unpack front application, debug");
        dto.setStatus(testStatus.getSlug());

        MockHttpServletRequestBuilder request = post("/api/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        Task task = taskRepository.findByNameAndIndex(
                dto.getTitle(),
                dto.getIndex()).get();

        assertThat(task).isNotNull();
        assertThat(task.getDescription()).isEqualTo(dto.getContent());

        taskRepository.delete(task);
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        TaskCreateDTO data = new TaskCreateDTO();
        data.setAssigneeId(testUser.getId());
        data.setTitle("Write CRUD for task");
        data.setContent("Write model, dto, mapper, repository, tests and controller");
        data.setStatus(testStatus.getSlug());

        MockHttpServletRequestBuilder request = put("/api/tasks/{id}", testTask.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Task task = taskRepository.findById(testTask.getId()).get();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(data.getTitle());
        assertThat(task.getDescription()).isEqualTo(data.getContent());
        assertThat(task.getAssignee().getId()).isEqualTo(data.getAssigneeId());

        taskRepository.delete(task);
    }

    @Test
    public void testDelete() throws Exception {
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/tasks/{id}", testTask.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(false);
    }

    @Test
    public void testDeleteWithoutAuthorization() throws Exception {
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/tasks/{id}", testStatus.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(true);

        taskRepository.delete(testTask);
    }
}
