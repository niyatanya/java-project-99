package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
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
import java.util.HashSet;
import java.util.Set;

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

    private final InstanceGenerator generator = new InstanceGenerator();

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

        testUser = generator.getUser();
        userRepository.save(testUser);

        testStatus = generator.getTaskStatus();
        statusRepository.save(testStatus);

        testLabel = generator.getLabel();
        labelRepository.save(testLabel);

        testTask = generator.getTask();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);

        Set<Label> labels = new HashSet<>();
        labels.add(testLabel);
        testTask.setLabels(labels);
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);

        MvcResult result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithFilter() throws Exception {
        Task testTask2 = generator.getTask();
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
        );
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
    }

    @Test
    public void testCreate() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setIndex(123);
        dto.setAssigneeId(testUser.getId());
        dto.setTitle("Add front to app");
        dto.setContent("Install npm, unpack front application, debug");
        dto.setStatus(testStatus.getSlug());
        dto.setTaskLabelIds(new HashSet<Long>(testTask.getLabels().stream().map(Label::getId).toList()));

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

    @Test
    public void testDeleteWithoutAuthorization() throws Exception {
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/tasks/{id}", testStatus.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(true);
    }
}
