package rs.vs.meetings_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.vs.meetings_service.client.AuthServiceClient;
import rs.vs.meetings_service.client.UserInfoDto;
import rs.vs.meetings_service.dto.*;
import rs.vs.meetings_service.exception.BusinessRuleException;
import rs.vs.meetings_service.model.*;
import rs.vs.meetings_service.repository.MeetingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private static final int MIN_DAYS_BEFORE_MEETING = 3;

    private final MeetingRepository meetingRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public Meeting createMeeting(Long organizerId, MeetingCreateRequest request) {
        UserInfoDto organizer = authServiceClient.getUser(organizerId);

        if(!organizer.getPrimaryRole().equals("ADMINISTRATOR") && !organizer.getPrimaryRole().equals("RUKOVODILAC")){
            throw new BusinessRuleException("Korisnik sa zadatom ulogom ne moze kreirati sastanak");
        }

        if(request.getScheduledDate().isBefore(LocalDate.now().plusDays(MIN_DAYS_BEFORE_MEETING))){
            throw new BusinessRuleException("Sastanak mora biti zakazan minimum 3 dana ranije");
        }

        if(request.getType() == MeetingType.STALNI && request.getFrequency() == null){
            throw new BusinessRuleException("Stalni sastanci moraju imati definisanu ucestalost");
        }

        UserInfoDto recorder = authServiceClient.getUser(request.getRecorderId());

        if(organizer.getOrganizationalUnitId() != null && recorder.getOrganizationalUnitId() != null
                && !organizer.getOrganizationalUnitId().equals(recorder.getOrganizationalUnitId())){
            throw new BusinessRuleException("Zapisnicar mora biti iz iste organizacione jedinice kao organizator");
        }

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .type(request.getType())
                .frequency(request.getFrequency())
                .locationType(request.getLocationType())
                .room(request.getRoom())
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .organizationalUnitId(organizer.getOrganizationalUnitId())
                .organizerId(organizer.getId())
                .recorderId(recorder.getId())
                .actNumber(request.getActNumber())
                .actDate(request.getActDate())
                .actIssuingOrganization(request.getActIssuingOrganization())
                .status(MeetingStatus.ZAKAZAN)
                .build();

        if(request.getAgendaItems() != null){
            List<AgendaItem> items = new ArrayList<>();
            int order = 1;
            for(AgendaItemRequest itemReq : request.getAgendaItems()){
                items.add(AgendaItem.builder()
                                .meeting(meeting).orderNum(order++)
                                .title(itemReq.getTitle())
                                .description(itemReq.getDescription())
                                .build());
            }
            meeting.setAgendaItems(items);
        }

        if(request.getParticipants() != null){
            List<Participant> participants = new ArrayList<>();
            for(ParticipantRequest parReq : request.getParticipants()){
                Participant.ParticipantBuilder builder = Participant.builder().meeting(meeting).plannedToAttend(true);
                if(parReq.getUserId() != null){
                    authServiceClient.getUser(parReq.getUserId());
                    builder.userId(parReq.getUserId());
                }else{
                    builder.externalFirstName(parReq.getExternalFirstName())
                            .externalLastName(parReq.getExternalLastName())
                            .externalOrganizationalUnit(parReq.getExternalOrganizationalUnit())
                            .externalJobTitle(parReq.getExternalJobTitle())
                            .externalCountry(parReq.getExternalCountry());
                }
                participants.add(builder.build());
            }
            meeting.setParticipants(participants);
        }

        return meetingRepository.save(meeting);
    }

    public MeetingDetailDto toDetailDto(Meeting meeting) {
        UserInfoDto organizer = authServiceClient.getUser(meeting.getOrganizerId());
        UserInfoDto recorder = authServiceClient.getUser(meeting.getRecorderId());

        List<AgendaItemDetailDto> items = meeting.getAgendaItems().stream()
                .map(ai -> new AgendaItemDetailDto(ai.getId(), ai.getOrderNum(), ai.getTitle(), ai.getDescription(), ai.getConclusion()))
                .collect(Collectors.toList());

        List<ParticipantDetailDto> participants = meeting.getParticipants().stream()
                .map(p -> {
                    if(p.getUserId() != null){
                        UserInfoDto u = authServiceClient.getUser(p.getUserId());
                        return new ParticipantDetailDto(p.getId(), u.fullName(), u.getOrganizationalUnitName(), p.isPlannedToAttend(), p.isActuallyAttended());
                    }
                    return new ParticipantDetailDto(p.getId(), p.getExternalFirstName() + " " + p.getExternalLastName(),
                            p.getExternalOrganizationalUnit(),p.isPlannedToAttend(), p.isActuallyAttended());
                })
                .collect(Collectors.toList());

        return new MeetingDetailDto(
                meeting.getId(), meeting.getTitle(), meeting.getCategory(), meeting.getType(), meeting.getFrequency(),
                meeting.getLocationType(), meeting.getRoom(), meeting.getScheduledDate(), meeting.getScheduledTime(), meeting.getStatus(),
                organizer.fullName(),recorder.fullName(), organizer.getOrganizationalUnitName(), items, participants
        );
    }

    public Page<MeetingSummaryDto> findVisibleToUser(Long userId, Pageable pageable) {
        return meetingRepository.findAllVisibleToUser(userId,pageable).map(this::toSummary);
    }

    private MeetingSummaryDto toSummary(Meeting meeting){
        UserInfoDto organizer = authServiceClient.getUser(meeting.getOrganizerId());
        return new MeetingSummaryDto(
                meeting.getId(), meeting.getTitle(), meeting.getCategory(),
                meeting.getStatus(), meeting.getScheduledDate(), meeting.getScheduledTime(),
                organizer.fullName(), organizer.getOrganizationalUnitName()
        );
    }
}
