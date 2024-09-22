package hexlet.code.controller.api;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.service.UserService;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserUtils userUtils;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> result = userService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.size()))
                .body(result);
    }

    @GetMapping(path = "/{id}")
    public UserDTO getById(@PathVariable long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO dto) {
        return userService.create(dto);
    }

    @PutMapping(path = "/{id}")
    public UserDTO update(@Valid @RequestBody UserUpdateDTO dto,
                           @PathVariable long id) throws AccessDeniedException {
        User currentUser = userUtils.getCurrentUser();
        if (currentUser.getId() != id) {
            throw new AccessDeniedException("Access denied.");
        }

        return userService.update(dto, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) throws AccessDeniedException {
        User currentUser = userUtils.getCurrentUser();
        if (currentUser.getId() != id) {
            throw new AccessDeniedException("Access denied.");
        }
        userService.deleteById(id);
    }
}
