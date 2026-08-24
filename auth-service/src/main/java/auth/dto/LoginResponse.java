package auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private Long userId;

    private String username;

    private Set<String> roles;
}
