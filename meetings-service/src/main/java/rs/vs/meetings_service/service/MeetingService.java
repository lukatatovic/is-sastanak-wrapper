package rs.vs.meetings_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.vs.meetings_service.client.AuthServiceClient;
import rs.vs.meetings_service.client.UserInfoDto;
import rs.vs.meetings_service.dto.*;
import rs.vs.meetings_service.exception.BusinessRuleException;
import rs.vs.meetings_service.exception.ResourceNotFoundReception;
import rs.vs.meetings_service.model.*;
import rs.vs.meetings_service.repository.MeetingRepository;
import rs.vs.meetings_service.repository.ParticipantRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private static final int MIN_DAYS_BEFORE_MEETING = 3;

    private final MeetingRepository meetingRepository;
    private final AuthServiceClient authServiceClient;
    private final ParticipantRepository participantRepository;
    private final NotificationService notificationService;

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

        Meeting savedMeeting = meetingRepository.save(meeting);

        if (recorder != null && !"ZAPISNICAR".equals(recorder.getPrimaryRole())) {
            authServiceClient.assignTemporaryRole(recorder.getId(),"ZAPISNICAR", savedMeeting.getId(),"Automatski dodeljena uloga zapisnicara za sastanak #" + savedMeeting.getId()
            );
        }

        List<Long> recipientIds = savedMeeting.getParticipants().stream()
                .map(Participant::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        recipientIds.add(savedMeeting.getRecorderId());

        notificationService.notifyMany(recipientIds,"Pozvani ste na sastanak: " +savedMeeting.getTitle()+ " (" + savedMeeting.getScheduledDate() +" )", "INFO", savedMeeting.getId());

        return savedMeeting;
    }

    public MeetingDetailDto toDetailDto(Meeting meeting) {
        updateExpiredMeetingStatus(meeting);
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
                organizer.fullName(),recorder.fullName(), organizer.getOrganizationalUnitName(), items, participants, meeting.getFinalConclusion()
        );
    }

    public Page<MeetingSummaryDto> findVisibleToUser(Long userId, Pageable pageable) {
        return meetingRepository.findAllVisibleToUser(userId,pageable).map(this::toSummary);
    }

    private MeetingSummaryDto toSummary(Meeting meeting){
        updateExpiredMeetingStatus(meeting);
        UserInfoDto organizer = authServiceClient.getUser(meeting.getOrganizerId());
        return new MeetingSummaryDto(
                meeting.getId(), meeting.getTitle(), meeting.getCategory(),
                meeting.getStatus(), meeting.getScheduledDate(), meeting.getScheduledTime(),
                organizer.fullName(), organizer.getOrganizationalUnitName()
        );
    }

    public Page<MeetingSummaryDto> findByOrganizationalUnit(Long orgUnitId, Pageable pageable) {
        return meetingRepository.findByOrganizationalUnitId(orgUnitId,pageable).map(this::toSummary);
    }

    public Meeting getOrThrow(Long id){
        return meetingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundReception("Sastanak ne postoji"));
    }

    @Transactional
    public Meeting postponeOrCancel(Long meetingId, MeetingStatus newStatus, String reason){
        if(newStatus != MeetingStatus.ODLOZEN && newStatus != MeetingStatus.OTKAZAN){
            throw new BusinessRuleException("Status mora biti odlozen ili otkazan");
        }

        if(reason == null || reason.isBlank()){
            throw new BusinessRuleException("Razlog promene statusa sastanka je obavezan");
        }

        Meeting meeting = getOrThrow(meetingId);
        meeting.setStatus(newStatus);
        meeting.setPostponeOrCancelReason(reason);

        List<Long> recipientIds = meeting.getParticipants().stream()
                .map(Participant::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        recipientIds.add(meeting.getRecorderId());

        notificationService.notifyMany(recipientIds,"Sastanak '" + meeting.getTitle() + "' je " +(newStatus == MeetingStatus.ODLOZEN ? "odložen" : "otkazan") + ": " + reason ,"WARNING", meeting.getId());

        return meetingRepository.save(meeting);
    }

    @Transactional
    public void recordAttendance(Long meetingId, List<AttendanceUpdateRequest> updates) {
        Meeting meeting = getOrThrow(meetingId);

        if(LocalDateTime.now().isBefore( meeting.getScheduledDate().atTime(meeting.getScheduledTime()))){
            throw new BusinessRuleException("Evidencija prisustva nije moguca pre datuma i vremena odrzavanja sastanka");
        }

        long hoursSinceMeeting = Duration.between(
                meeting.getScheduledDate().atTime(meeting.getScheduledTime()),
                LocalDateTime.now()).toHours();

        if(hoursSinceMeeting > 72){
            throw new BusinessRuleException("Evidencija prisustva moguca je najvise 72 sata nakon sastanka");
        }

        List<Long> attendedUserIds = new ArrayList<>();

        for(AttendanceUpdateRequest u : updates){
            Participant p = participantRepository.findById(u.getParticipantId()).orElseThrow(() -> new ResourceNotFoundReception("Ucesnik ne postoji"));

            if(!p.getMeeting().getId().equals(meetingId)){
                throw new BusinessRuleException("Ucesnik ne pripada ovom sastanku");
            }
            p.setActuallyAttended(u.isActuallyAttended());
            if(p.isActuallyAttended()) {
                attendedUserIds.add(p.getUserId());
            }
        }
        meeting.setStatus(MeetingStatus.ODRZAN);

        notificationService.notifyMany(attendedUserIds,"Vase prisustvo na sastanku '" + meeting.getTitle() + "' je evidentirano.","SUCCESS", meeting.getId());
    }

    private void updateExpiredMeetingStatus(Meeting meeting) {
        if (meeting.getStatus() != MeetingStatus.ZAKAZAN) {
            return;
        }

        LocalDateTime scheduledDateTime = meeting.getScheduledDate()
                .atTime(meeting.getScheduledTime());

        long hoursSinceMeeting = Duration.between(
                scheduledDateTime,
                LocalDateTime.now()
        ).toHours();

        if (hoursSinceMeeting > 72) {
            meeting.setStatus(MeetingStatus.OTKAZAN);
            meeting.setPostponeOrCancelReason("Nepoznat razlog");
            meetingRepository.save(meeting);
        }
    }


    public Meeting updateAgenda(Long id, MeetingAgendaUpdateRequest request) {
        Meeting meeting = getOrThrow(id);

        if(LocalDateTime.now().isBefore( meeting.getScheduledDate().atTime(meeting.getScheduledTime()))){
            throw new BusinessRuleException("Promena stavki dnevnog reda i unosenje zakljucka nije moguca pre datuma i vremena odrzavanja sastanka");
        }

        long hoursSinceMeeting = Duration.between(
                meeting.getScheduledDate().atTime(meeting.getScheduledTime()),
                LocalDateTime.now()).toHours();

        if(hoursSinceMeeting > 72){
            throw new BusinessRuleException("Promena stavki dnevnog reda i unosenje zakljucka je moguca do 72 sata nakon zavrsetka sastanka");
        }

        meeting.setFinalConclusion(request.getFinalConclusion());

        if(request.getAgendaItems() != null){
            for(AgendaItemDetailDto ai : request.getAgendaItems()){
                meeting.getAgendaItems().stream()
                        .filter(item -> item.getId().equals(ai.getId()))
                        .findFirst()
                        .ifPresent(item -> item.setDescription(ai.getDescription()));
            }
        }

        meeting.setStatus(MeetingStatus.ODRZAN);

        return meetingRepository.save(meeting);
    }
}
