package rs.vs.meetings_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceUpdateRequest {

    private Long participantId;

    private boolean actuallyAttended;
}
