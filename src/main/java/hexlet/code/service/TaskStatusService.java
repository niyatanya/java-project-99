package hexlet.code.service;

import hexlet.code.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskstatus.TaskStatusDTO;
import hexlet.code.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatusService {

    private final TaskStatusRepository statusRepository;
    private final TaskStatusMapper mapper;

    public List<TaskStatusDTO> getAll() {
        List<TaskStatus> statuses = statusRepository.findAll();
        return mapper.mapList(statuses);
    }

    public TaskStatusDTO getById(long id) {
        TaskStatus status = statusRepository.findById(id).orElseThrow();
        return mapper.map(status);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        TaskStatus status = mapper.map(data);
        statusRepository.save(status);
        return mapper.map(status);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO data, long id) {
        TaskStatus status = statusRepository.findById(id).orElseThrow();
        mapper.update(data, status);
        statusRepository.save(status);
        return mapper.map(status);
    }

    public void deleteById(long id) {
        statusRepository.deleteById(id);
    }
}
