package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.InstanceGenerator;
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
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private LabelRepository labelRepository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private ObjectMapper om;

    private Task testTask;

    private TaskStatus testStatus;

    private User testUser;

    private Label testLabel;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        statusRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testUser = InstanceGenerator.getUser();
        userRepository.save(testUser);

        testStatus = InstanceGenerator.getTaskStatus();
        statusRepository.save(testStatus);

        testLabel = InstanceGenerator.getLabel();
        labelRepository.save(testLabel);

        testTask = InstanceGenerator.getTask();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);

        Set<Label> labels = new HashSet<>();
        labels.add(testLabel);
        testTask.setLabels(labels);
    }

    @Test
    public void testGetAll() throws Exception {
        taskRepository.save(testTask);

        MvcResult result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testGetAllWithFilter() throws Exception {
        Task testTask2 = InstanceGenerator.getTask();
        testTask2.setTaskStatus(testStatus);

        taskRepository.save(testTask);
        taskRepository.save(testTask2);

        MvcResult result = mockMvc.perform(
                get("/api/tasks?titleCont={titleCont}&assigneeId={id}",
                        testTask.getName().substring(0, 3),
                        testUser.getId())
                            .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().allSatisfy(element ->
                        assertThatJson(element)
                                .and(v -> v.node("title").asString().containsIgnoringCase(testTask.getName()))
                                .and(v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()))
                                .and(v -> v.node("title").asString().doesNotContainIgnoringCase(testTask2.getName()))
        );
    }

    @Test
    public void testGetById() throws Exception {
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(jwt());

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        int index = testTask.getCreatedAt().toString().indexOf(".");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testTask.getId()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("createdAt").asString().contains(testTask.getCreatedAt()
                        .format(DateTimeFormatter.ISO_DATE_TIME).substring(0, index)),
                v -> v.node("taskLabelIds").isArray().containsExactlyInAnyOrder(
                        testTask.getLabels().stream()
                        .map(Label::getId)
                        .toArray())
        );
    }

    @Test
    public void testCreate() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setAssigneeId(testUser.getId());
        dto.setTitle(testTask.getName());
        dto.setContent(testTask.getDescription());
        dto.setStatus(testStatus.getSlug());
        dto.setAssigneeId(testUser.getId());
        dto.setTaskLabelIds(testTask.getLabels().stream()
                .map(Label::getId)
                .collect(Collectors.toSet()));

        MockHttpServletRequestBuilder request = post("/api/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        Task task = taskRepository.findByName(testTask.getName()).orElseThrow();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(dto.getTitle());
        assertThat(task.getDescription()).isEqualTo(dto.getContent());
        assertThat(task.getAssignee().getId()).isEqualTo(dto.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
        assertThat(task.getLabels().contains(testLabel));
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        TaskCreateDTO data = new TaskCreateDTO();
        data.setTitle("Write CRUD for task");
        data.setContent("Write model, dto, mapper, repository, tests and controller");

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
    }

    @Test
    public void testDelete() throws Exception {
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/tasks/{id}", testTask.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(false);
    }
}
