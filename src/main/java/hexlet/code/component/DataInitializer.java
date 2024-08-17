package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import hexlet.code.model.User;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private final CustomUserDetailsService userService;

    @Override
    public void run(ApplicationArguments args) {
        String email = "hexlet@example.com";
        User userData = new User();
        userData.setEmail(email);
        userData.setPasswordDigest("qwerty");
        userService.createUser(userData);

        TaskStatus status1 = new TaskStatus("черновик", "draft");
        TaskStatus status2 = new TaskStatus("на проверку", "to_review");
        TaskStatus status3 = new TaskStatus("на доработку", "to_be_fixed");
        TaskStatus status4 = new TaskStatus("к публикации", "to_publish");
        TaskStatus status5 = new TaskStatus("опубликовано", "published");
        statusRepository.save(status1);
        statusRepository.save(status2);
        statusRepository.save(status3);
        statusRepository.save(status4);
        statusRepository.save(status5);

        Label label1 = new Label("feature");
        Label label2 = new Label("bug");
        labelRepository.save(label1);
        labelRepository.save(label2);
    }
}
