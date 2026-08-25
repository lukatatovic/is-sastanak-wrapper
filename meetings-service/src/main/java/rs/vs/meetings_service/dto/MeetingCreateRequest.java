package rs.vs.meetings_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.vs.meetings_service.model.MeetingCategory;
import rs.vs.meetings_service.model.MeetingFrequency;
import rs.vs.meetings_service.model.MeetingLocationType;
import rs.vs.meetings_service.model.MeetingType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class MeetingCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private MeetingCategory category;

    @NotNull
    private MeetingType type;

    private MeetingFrequency frequency;

    @NotNull
    private MeetingLocationType locationType;

    private String room;

    @NotNull
    private LocalDate scheduledDate;

    @NotNull
    private LocalTime scheduledTime;

    @NotNull
    private Long recorderId;

    private String actNumber;

    private LocalDate actDate;

    private String actIssuingOrganization;

    @Valid
    private List<AgendaItemRequest> agendaItems;

    @Valid
    private List<ParticipantRequest> participants;
}
