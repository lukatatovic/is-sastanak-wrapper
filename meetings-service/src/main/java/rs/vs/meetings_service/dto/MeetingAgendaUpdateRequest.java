package rs.vs.meetings_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeetingAgendaUpdateRequest {

    private List<AgendaItemDetailDto> agendaItems;

    private String finalConclusion;
}
