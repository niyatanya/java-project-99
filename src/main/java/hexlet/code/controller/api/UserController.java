package hexlet.code.controller.api;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.security.access.AccessDeniedException;

import javax.naming.NoPermissionException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtils userUtils;

    @GetMapping
    private ResponseEntity<List<UserDTO>> getAll() {
        List<User> users = userRepository.findAll();
        List<UserDTO> result = users.stream()
                .map(mapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    private UserDTO getById(@PathVariable long id) {
        User user = userRepository.findById(id).orElseThrow();
        return mapper.map(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private UserDTO create(@Valid @RequestBody UserCreateDTO dto) {
        User user = mapper.map(dto);
        userRepository.save(user);
        return mapper.map(user);
    }

    @PutMapping(path = "/{id}")
    //value = "@userService.findById(#id).getEmail() == authentication.name"
    //"@userRepository.findById(#id).getEmail() == authentication.principal.username"
    //@PreAuthorize(value = "@userRepository.findById(#id).getEmail() == authentication.name")
    private UserDTO update(@Valid @RequestBody UserUpdateDTO dto,
                           @PathVariable long id) throws AccessDeniedException {
        User currentUser = userUtils.getCurrentUser();
        if (currentUser.getId() != id) {
            throw new AccessDeniedException("Access denied.");
        }

        return userService.updateUser(dto, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize(value = "@userRepository.findById(#id).getEmail() == authentication.principal.username")
    private void delete(@PathVariable long id) throws AccessDeniedException {
        User currentUser = userUtils.getCurrentUser();
        if (currentUser.getId() != id) {
            throw new AccessDeniedException("Access denied.");
        }
        userRepository.deleteById(id);
    }
}
