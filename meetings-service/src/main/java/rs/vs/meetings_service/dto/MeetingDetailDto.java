package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rs.vs.meetings_service.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingDetailDto {

    private Long id;

    private String title;

    private MeetingCategory category;

    private MeetingType type;

    private MeetingFrequency frequency;

    private MeetingLocationType locationType;

    private String room;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private MeetingStatus status;

    private String organizerFullName;

    private String recorderFullName;

    private String organizationalUnitName;

    private List<AgendaItemDetailDto> agendaItems;

    private List<ParticipantDetailDto> participants;

    private String finalConclusion;
}
