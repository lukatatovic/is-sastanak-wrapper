package rs.vs.meetings_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.vs.meetings_service.client.AuthServiceClient;
import rs.vs.meetings_service.client.UserInfoDto;
import rs.vs.meetings_service.dto.AgendaItemReportDto;
import rs.vs.meetings_service.dto.DiscussionEntryDto;
import rs.vs.meetings_service.dto.MeetingReportDto;
import rs.vs.meetings_service.model.AgendaItem;
import rs.vs.meetings_service.model.DiscussionEntry;
import rs.vs.meetings_service.model.Meeting;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MeetingService meetingService;
    private final AuthServiceClient authServiceClient;
    @Transactional(readOnly = true)
    public MeetingReportDto shortReport(Long meetingId) {
        Meeting meeting =meetingService.getOrThrow(meetingId);
        List<AgendaItemReportDto> items = meeting.getAgendaItems().stream()
                .map(ai -> new AgendaItemReportDto(ai.getOrderNum(), ai.getTitle(),null,null, ai.getConclusion()))
                .collect(Collectors.toList());

        return buildDto(meeting,items,false);
    }

    @Transactional(readOnly = true)
    public MeetingReportDto fullReport(Long meetingId) {
        Meeting meeting =meetingService.getOrThrow(meetingId);
        List<AgendaItemReportDto> items = meeting.getAgendaItems().stream()
                .map(this::toFullAgendaDto)
                .collect(Collectors.toList());

        return buildDto(meeting,items,true);
    }

    private MeetingReportDto buildDto(Meeting meeting, List<AgendaItemReportDto> items, boolean full){
        UserInfoDto organizer = authServiceClient.getUser(meeting.getOrganizerId());
        UserInfoDto recorder = authServiceClient.getUser(meeting.getRecorderId());

        return new MeetingReportDto( meeting.getId(),meeting.getTitle(),meeting.getCategory(), meeting.getScheduledDate(),
                meeting.getScheduledTime(),organizer.fullName(),recorder.fullName(),items, meeting.getFinalConclusion(),full);
    }

    private AgendaItemReportDto toFullAgendaDto(AgendaItem ai){
        List<DiscussionEntryDto> entries = ai.getDiscussionEntries().stream()
                .map(this:: toDiscussionDto)
                .collect(Collectors.toList());

        return new AgendaItemReportDto(ai.getOrderNum(), ai.getTitle(), ai.getDescription(), entries, ai.getConclusion());
    }

    private DiscussionEntryDto toDiscussionDto(DiscussionEntry d){
        String speaker;
        if(d.getSpeakerUserId() != null){
            UserInfoDto u = authServiceClient.getUser(d.getSpeakerUserId());
            speaker = u.fullName();
        }else{
            speaker = d.getSpeakerExternalName();
        }

        return new DiscussionEntryDto(speaker, d.getContent(), d.getTimestamp());
    }
}
