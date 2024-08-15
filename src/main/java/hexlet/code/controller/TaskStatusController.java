package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.EntityCanNotBeDeletedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper mapper;

    @GetMapping
    public ResponseEntity<List<TaskStatus>> index() {
        List<TaskStatus> statuses = statusRepository.findAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(statuses);
    }

    @GetMapping(path = "/{id}")
    private TaskStatus show(@PathVariable long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with " + id + " not found."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private TaskStatus create(@Valid @RequestBody TaskStatusCreateDTO data) {
        TaskStatus status = mapper.map(data);
        statusRepository.save(status);
        return status;
    }

    @PutMapping(path = "/{id}")
    public TaskStatus update(@Valid @RequestBody TaskStatusUpdateDTO data,
                             @PathVariable long id) {
        TaskStatus status = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with " + id + " not found."));
        mapper.update(data, status);
        statusRepository.save(status);
        return status;
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        TaskStatus status = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with " + id + " not found."));

        if (status.getTasks().isEmpty()) {
            statusRepository.deleteById(id);
        } else {
            throw new EntityCanNotBeDeletedException("Task status can not be deleted while at least one task has it.");
        }
    }
}
