package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantDetailDto {

    private Long id;

    private String fullName;

    private String organizationalUnitOrCompany;

    private boolean plannedToAttend;

    private boolean actuallyAttended;
}
