package auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UserInternalDto {

    private Long id;

    private String firstName;

    private String lastName;

    private Long organizationalUnitId;

    private String organizationalUnitName;

    private String primaryRole;

    private Set<String> effectiveRoles;;
}
