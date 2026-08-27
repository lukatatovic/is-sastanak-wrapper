package rs.vs.export_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingReportDto {

    private Long meetingId;

    private String title;

    private String category;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private String organizerFullName;

    private String recorderFullName;

    List<AgendaItemReportDto> agendaItems;

    private String finalConclusion;

    private boolean fullReport;
}
