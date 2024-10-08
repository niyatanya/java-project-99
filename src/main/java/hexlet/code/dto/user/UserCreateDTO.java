package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UserCreateDTO {
    @NotNull
    @Email
    private String email;

    private String firstName;

    private String lastName;

    @NotNull
    @Size(min = 3)
    private String password;
}
