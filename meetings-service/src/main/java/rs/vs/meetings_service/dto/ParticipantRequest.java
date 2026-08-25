package rs.vs.meetings_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantRequest {

    private Long userId;

    private String externalFirstName;

    private String externalLastName;

    private String externalOrganizationalUnit;

    private String externalJobTitle;

    private String externalCountry;
}
