package hexlet.code.service;

import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    public UserDTO updateUser(@RequestBody UserUpdateDTO dto,
                           @PathVariable long id) {
        User user = userRepository.findById(id).orElseThrow();
        mapper.update(dto, user);
        userRepository.save(user);
        return mapper.map(user);
    }
}
