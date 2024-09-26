package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public List<UserDTO> getAll() {
        List<User> users = userRepository.findAll();
        return mapper.mapList(users);
    }

    public UserDTO getById(long id) {
        User user = userRepository.findById(id).orElseThrow();
        return mapper.map(user);
    }

    public UserDTO create(UserCreateDTO dto) {
        User user = mapper.map(dto);
        userRepository.save(user);
        return mapper.map(user);
    }

    public UserDTO update(UserUpdateDTO dto, long id) {
        User user = userRepository.findById(id).orElseThrow();
        mapper.update(dto, user);
        userRepository.save(user);
        return mapper.map(user);
    }

    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    @Override
    public void createUser(UserDetails userData) {
        User user = new User();
        user.setEmail(userData.getUsername());
        var hashedPassword = passwordEncoder.encode(userData.getPassword());
        user.setPasswordDigest(hashedPassword);
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails user) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("Unimplemented method 'changePassword'");
    }

    @Override
    public boolean userExists(String username) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("Unimplemented method 'userExists'");
    }
}
