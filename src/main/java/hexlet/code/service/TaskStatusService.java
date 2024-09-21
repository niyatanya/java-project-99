package hexlet.code.service;

import hexlet.code.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskstatus.TaskStatusDTO;
import hexlet.code.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper mapper;

    public List<TaskStatusDTO> getAll() {
        List<TaskStatus> statuses = statusRepository.findAll();
        return statuses.stream()
                .map(mapper::map)
                .toList();
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
