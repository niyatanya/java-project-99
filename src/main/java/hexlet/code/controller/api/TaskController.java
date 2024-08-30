package hexlet.code.controller.api;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;

import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import jakarta.validation.Valid;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private TaskSpecification specBuilder;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAll(TaskParamsDTO params,
                                               @RequestParam(defaultValue = "1") int page) {
        Specification<Task> spec = specBuilder.build(params);
        List<Task> tasks = taskRepository.findAll(spec, PageRequest.of(page - 1, 10)).toList();
        List<TaskDTO> result = tasks.stream()
                .map(mapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    private TaskDTO getById(@PathVariable long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        return mapper.map(task);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private TaskDTO create(@Valid @RequestBody TaskCreateDTO data) {
        Task task = mapper.map(data);
        taskRepository.save(task);
        return mapper.map(task);
    }

    @PutMapping(path = "/{id}")
    public TaskDTO update(@Valid @RequestBody TaskUpdateDTO data,
                             @PathVariable long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        mapper.update(data, task);
        taskRepository.save(task);
        return mapper.map(task);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskRepository.deleteById(id);
    }
}
