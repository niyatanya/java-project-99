package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import hexlet.code.model.User;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final TaskStatusRepository statusRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        String email = "hexlet@example.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            User userData = new User();
            userData.setEmail(email);
            userData.setPasswordDigest("qwerty");
            userService.createUser(userData);
        }

        List<TaskStatus> statuses = new ArrayList<>(List.of(
                new TaskStatus("черновик", "draft"),
                new TaskStatus("на проверку", "to_review"),
                new TaskStatus("на доработку", "to_be_fixed"),
                new TaskStatus("к публикации", "to_publish"),
                new TaskStatus("опубликовано", "published")));

        for (TaskStatus status : statuses) {
            if (statusRepository.findByName(status.getName()).isEmpty()) {
                statusRepository.save(status);
            }
        }

        List<Label> labels = new ArrayList<>(List.of(
                new Label("feature"),
                new Label("bug")));

        for (Label label : labels) {
            if (labelRepository.findByName(label.getName()).isEmpty()) {
                labelRepository.save(label);
            }
        }
    }
}
