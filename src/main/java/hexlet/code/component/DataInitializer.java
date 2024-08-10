package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private final CustomUserDetailsService userService;

    @Override
    public void run(ApplicationArguments args) {
        String email = "hexlet@example.com";
        User userData = new User();
        userData.setEmail(email);
        userData.setPasswordDigest("qwerty");
        userService.createUser(userData);

        TaskStatus status1 = new TaskStatus();
        status1.setName("черновик");
        status1.setSlug("draft");
        TaskStatus status2 = new TaskStatus();
        status2.setName("на проверку");
        status2.setSlug("to_review");
        TaskStatus status3 = new TaskStatus();
        status3.setName("на доработку");
        status3.setSlug("to_be_fixed");
        TaskStatus status4 = new TaskStatus();
        status4.setName("к публикации");
        status4.setSlug("to_publish");
        TaskStatus status5 = new TaskStatus();
        status5.setName("опубликовано");
        status5.setSlug("published");
        statusRepository.save(status1);
        statusRepository.save(status2);
        statusRepository.save(status3);
        statusRepository.save(status4);
        statusRepository.save(status5);
    }
}
