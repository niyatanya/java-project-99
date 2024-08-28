package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.LabelRepository;
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
public class LabelControllerTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private LabelMapper mapper;

    @Autowired
    private ObjectMapper om;

    private Label testLabel;

    private final InstanceGenerator generator = new InstanceGenerator();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testLabel = generator.getLabel();
    }

    @Test
    public void testIndex() throws Exception {
        labelRepository.save(testLabel);

        MvcResult result = mockMvc.perform(get("/api/labels").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();

        labelRepository.delete(testLabel);
    }

    @Test
    public void testShow() throws Exception {
        labelRepository.save(testLabel);

        var request = get("/api/labels/{id}", testLabel.getId()).with(jwt());

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );

        labelRepository.delete(testLabel);
    }

    @Test
    public void testCreate() throws Exception {
        LabelCreateDTO dto = new LabelCreateDTO();
        dto.setName(testLabel.getName());

        MockHttpServletRequestBuilder request = post("/api/labels")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        Label label = labelRepository.findByName(testLabel.getName()).get();
        assertThat(label).isNotNull();

        labelRepository.delete(testLabel);
    }

    @Test
    public void testCreateWithNotValidName() throws Exception {
        LabelCreateDTO dto = new LabelCreateDTO();
        dto.setName("aa");

        MockHttpServletRequestBuilder request = post("/api/labels")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        labelRepository.save(testLabel);

        Label data = new Label();
        data.setName("New name");

        MockHttpServletRequestBuilder request = put("/api/labels/{id}", testLabel.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Label label = labelRepository.findById(testLabel.getId()).get();

        assertThat(label).isNotNull();
        assertThat(label.getName()).isEqualTo(data.getName());

        labelRepository.delete(testLabel);
    }

    @Test
    public void testDelete() throws Exception {
        labelRepository.save(testLabel);

        MockHttpServletRequestBuilder request = delete("/api/labels/{id}", testLabel.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(labelRepository.existsById(testLabel.getId())).isEqualTo(false);
    }

    @Test
    public void testDeleteWithoutAuthorization() throws Exception {
        labelRepository.save(testLabel);

        MockHttpServletRequestBuilder request = delete("/api/labels/{id}", testLabel.getId());

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(labelRepository.existsById(testLabel.getId())).isEqualTo(true);

        labelRepository.delete(testLabel);
    }

    @Test
    public void testDeleteLabelWithTask() throws Exception {
        labelRepository.save(testLabel);

        TaskStatus testStatus = generator.getTaskStatus();
        statusRepository.save(testStatus);

        Task testTask = generator.getTask();
        testTask.setTaskStatus(testStatus);
        testTask.getLabels().add(testLabel);
        taskRepository.save(testTask);

        MockHttpServletRequestBuilder request = delete("/api/labels/{id}", testLabel.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isLocked());

        assertThat(labelRepository.existsById(testLabel.getId())).isEqualTo(true);

        taskRepository.delete(testTask);
        labelRepository.delete(testLabel);
    }
}
