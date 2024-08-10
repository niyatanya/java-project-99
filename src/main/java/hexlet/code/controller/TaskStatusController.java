package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.NoPermissionToAccessException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.UserUtils;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper mapper;

    @Autowired
    private UserUtils userUtils;

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
        if (userUtils.isAuthenticated()) {
            TaskStatus status = mapper.map(data);
            statusRepository.save(status);
            return status;
        } else {
            throw new NoPermissionToAccessException("No permission to change task statuses");
        }
    }

    @PutMapping(path = "/{id}")
    public TaskStatus update(@Valid @RequestBody TaskStatusUpdateDTO data,
                             @PathVariable long id) {
        if (userUtils.isAuthenticated()) {
            TaskStatus status = statusRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status with " + id + " not found."));
            mapper.update(data, status);
            statusRepository.save(status);
            return status;
        } else {
            throw new NoPermissionToAccessException("No permission to change task statuses");
        }
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        if (userUtils.isAuthenticated()) {
            statusRepository.deleteById(id);
        } else {
            throw new NoPermissionToAccessException("No permission to change task statuses");
        }
    }
}
