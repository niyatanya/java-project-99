package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
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
import java.util.HashMap;
import java.util.Map;

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
public class TaskStatusesControllerTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusMapper mapper;

    @Autowired
    private ObjectMapper om;

    private TaskStatus testStatus;

    private final InstanceGenerator generator = new InstanceGenerator();

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        statusRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testStatus = generator.getTaskStatus();
    }

    @Test
    public void testIndex() throws Exception {
        statusRepository.save(testStatus);

        MvcResult result = mockMvc.perform(get("/api/task_statuses").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        statusRepository.save(testStatus);

        var request = get("/api/task_statuses/{id}", testStatus.getId()).with(jwt());

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        int index = testStatus.getCreatedAt().toString().indexOf(".");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testStatus.getId()),
                v -> v.node("name").isEqualTo(testStatus.getName()),
                v -> v.node("slug").isEqualTo(testStatus.getSlug()),
                v -> v.node("createdAt").asString().contains(testStatus.getCreatedAt()
                        .format(DateTimeFormatter.ISO_DATE_TIME).substring(0, index))
        );
    }

    @Test
    public void testCreate() throws Exception {
        TaskStatusCreateDTO dto = new TaskStatusCreateDTO();
        dto.setName(testStatus.getName());
        dto.setSlug(testStatus.getSlug());

        MockHttpServletRequestBuilder request = post("/api/task_statuses")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        TaskStatus status = statusRepository.findBySlug(testStatus.getSlug()).get();

        assertThat(status).isNotNull();
        assertThat(status.getName()).isEqualTo(testStatus.getName());
    }

    @Test
    public void testCreateWithNotValidSlug() throws Exception {
        TaskStatusCreateDTO dto = new TaskStatusCreateDTO();
        dto.setName(testStatus.getName());
        dto.setSlug("");

        MockHttpServletRequestBuilder request = post("/api/task_statuses")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        statusRepository.save(testStatus);

        TaskStatus data = new TaskStatus();
        data.setName("New name");
        data.setSlug("New slug");

        MockHttpServletRequestBuilder request = put("/api/task_statuses/{id}", testStatus.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        TaskStatus status = statusRepository.findById(testStatus.getId()).get();

        assertThat(status).isNotNull();
        assertThat(status.getName()).isEqualTo(data.getName());
        assertThat(status.getSlug()).isEqualTo(data.getSlug());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        statusRepository.save(testStatus);

        Map<String, String> dto = new HashMap<>();
        dto.put("name", "Another name");

        MockHttpServletRequestBuilder request = put("/api/task_statuses/{id}", testStatus.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        TaskStatus status = statusRepository.findById(testStatus.getId()).get();

        assertThat(status).isNotNull();
        assertThat(status.getName()).isEqualTo(dto.get("name"));
        assertThat(status.getSlug()).isEqualTo(testStatus.getSlug());
    }

    @Test
    public void testDelete() throws Exception {
        statusRepository.save(testStatus);

        MockHttpServletRequestBuilder request = delete("/api/task_statuses/{id}", testStatus.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(statusRepository.existsById(testStatus.getId())).isEqualTo(false);
    }

    @Test
    public void testDeleteTaskStatusWithTask() throws Exception {
        statusRepository.save(testStatus);
        Task testTask = generator.getTask();
        testTask.setTaskStatus(testStatus);
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/task_statuses/{id}", testStatus.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isLocked());

        assertThat(statusRepository.existsById(testStatus.getId())).isEqualTo(true);
    }
}
