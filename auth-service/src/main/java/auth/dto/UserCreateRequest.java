package auth.dto;

import auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank
    private String firstName;

    private String fatherName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String jmbg;

    private String jobTitle;

    private String officePhone;

    private String mobilePhone;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Long organizationalUnitId;

    @NotNull
    private Role primaryRole;
}
