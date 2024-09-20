package hexlet.code.controller.api;

import hexlet.code.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskstatus.TaskStatusDTO;
import hexlet.code.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    final TaskStatusRepository statusRepository;

    final TaskRepository taskRepository;

    final TaskStatusMapper mapper;

    @GetMapping
    public ResponseEntity<List<TaskStatusDTO>> getAll() {
        List<TaskStatus> statuses = statusRepository.findAll();
        List<TaskStatusDTO> result = statuses.stream()
                .map(mapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    private TaskStatusDTO getById(@PathVariable long id) {
         TaskStatus status = statusRepository.findById(id).orElseThrow();
        return mapper.map(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO data) {
        TaskStatus status = mapper.map(data);
        statusRepository.save(status);
        return mapper.map(status);
    }

    @PutMapping(path = "/{id}")
    public TaskStatusDTO update(@Valid @RequestBody TaskStatusUpdateDTO data,
                             @PathVariable long id) {
        TaskStatus status = statusRepository.findById(id).orElseThrow();
        mapper.update(data, status);
        statusRepository.save(status);
        return mapper.map(status);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        statusRepository.deleteById(id);
    }
}
