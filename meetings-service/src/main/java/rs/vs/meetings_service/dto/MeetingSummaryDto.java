package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rs.vs.meetings_service.model.MeetingCategory;
import rs.vs.meetings_service.model.MeetingStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class MeetingSummaryDto {

    private Long id;

    private String title;

    private MeetingCategory category;

    private MeetingStatus status;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private String organizerFullName;

    private String organizationalUnitName;
}
