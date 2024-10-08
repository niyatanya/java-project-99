package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper mapper;
    private final TaskSpecification specBuilder;

    public List<TaskDTO> getAll(TaskParamsDTO params) {
        Specification<Task> spec = specBuilder.build(params);
        List<Task> tasks = taskRepository.findAll(spec);
        return mapper.mapList(tasks);
    }

    public TaskDTO getById(long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        return mapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO data) {
        Task task = mapper.map(data);
        taskRepository.save(task);
        return mapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO data, long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        mapper.update(data, task);
        taskRepository.save(task);
        return mapper.map(task);
    }

    public void deleteById(long id) {
        taskRepository.deleteById(id);
    }
}
