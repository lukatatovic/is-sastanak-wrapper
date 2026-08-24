package auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String jobTitle;

    private String email;

    private String username;

    private Long organizationalUnitId;

    private String organizationalUnitName;

    private String primaryRole;
}
