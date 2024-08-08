package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.NoPermissionToAccessException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.util.UserUtils;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private UserService userService;

    @GetMapping
    private ResponseEntity<List<UserDTO>> index() {
        List<User> users = userRepository.findAll();
        List<UserDTO> result = users.stream()
                .map(mapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    private UserDTO show(@PathVariable long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));
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
    private UserDTO update(@Valid @RequestBody UserUpdateDTO dto,
                           @PathVariable long id) {

        UserUtils utils = new UserUtils();
        User currentUser = utils.getCurrentUser();
        User userToDelete = userRepository.findById(id).get();

        if (currentUser.getId() == userToDelete.getId()) {
            return userService.updateUser(dto, id);
        } else {
            throw new NoPermissionToAccessException("No permission to change other users details");
        }
    }

    @DeleteMapping(path = "/{id}")
    private void delete(@PathVariable long id) {
        UserUtils utils = new UserUtils();
        User currentUser = utils.getCurrentUser();
        User userToDelete = userRepository.findById(id).get();

        if (currentUser.getId() == userToDelete.getId()) {
            userRepository.deleteById(id);
        } else {
            throw new NoPermissionToAccessException("No permission to delete other users");
        }
    }
}
