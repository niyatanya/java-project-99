package hexlet.code.controller.api;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.EntityCanNotBeDeletedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.LabelRepository;

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
@RequestMapping("/api/labels")
public class LabelController {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelMapper mapper;

    @GetMapping
    public ResponseEntity<List<LabelDTO>> index() {
        List<Label> labels = labelRepository.findAll();
        List<LabelDTO> result = labels.stream()
                .map(mapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    private LabelDTO show(@PathVariable long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with " + id + " not found."));
        return mapper.map(label);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private LabelDTO create(@Valid @RequestBody LabelCreateDTO data) {
        Label label = mapper.map(data);
        labelRepository.save(label);
        return mapper.map(label);
    }

    @PutMapping(path = "/{id}")
    public LabelDTO update(@Valid @RequestBody LabelUpdateDTO data,
                             @PathVariable long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task label with " + id + " not found."));
        mapper.update(data, label);
        labelRepository.save(label);
        return mapper.map(label);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) throws Exception {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with " + id + " not found."));

        List<Task> tasks = taskRepository.findAll();
        List<Task> tasksWithLabel = tasks.stream()
                .filter(t -> t.getLabels().contains(label))
                .toList();

        if (tasksWithLabel.isEmpty()) {
            labelRepository.deleteById(id);
        } else {
            throw new EntityCanNotBeDeletedException("Label can not be deleted while at least one task has it.");
        }
    }
}
