package auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TemporaryRoleAssignmentDto {

    private Long id;

    private Long userId;

    private String userFullName;

    private String role;

    private Long meetingId;

    private Long organizationalUnitId;

    private String note;

    private String assignedByAdminName;

    private LocalDateTime assignedAt;

    private LocalDateTime validUntil;

    private boolean revoked;

    private boolean active;

}
