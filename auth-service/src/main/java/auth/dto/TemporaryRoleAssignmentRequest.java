package auth.dto;

import auth.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TemporaryRoleAssignmentRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Role role;

    private Long meetingId;

    private Long organizationalUnitId;

    @NotBlank
    private String note;

    private LocalDateTime validUntil;
}
