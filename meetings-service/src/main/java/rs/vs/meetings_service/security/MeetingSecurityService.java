package rs.vs.meetings_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.vs.meetings_service.client.AuthServiceClient;
import rs.vs.meetings_service.model.Meeting;
import rs.vs.meetings_service.repository.MeetingRepository;

@Service
@RequiredArgsConstructor
public class MeetingSecurityService {

    private final MeetingRepository meetingRepository;
    private final AuthServiceClient authServiceClient;

    public boolean isAdmin(Authentication auth) { return  hasRole(auth,"ADMINISTRATOR");}

    @Transactional(readOnly = true)
    public boolean canViewMeeting(Authentication auth, Long meetingId){
        Long userId = principalId(auth);
        if(hasRole(auth,"ADMINISTRATOR")) return true;

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) return false;

        if(meeting.getOrganizerId().equals(userId)) return true;
        if(meeting.getRecorderId().equals(userId)) return true;

        boolean isParticipant = meeting.getParticipants().stream()
                .anyMatch(p -> p.getUserId() != null && p.getUserId().equals(userId));

        if (isParticipant) return true;

        return hasAnyEffectiveRole(userId,meeting,"RUKOVODILAC", "ZAPISNICAR");

    }

    public boolean canManageMeeting(Authentication auth, Long meetingId){
        Long userId = principalId(auth);
        if(hasRole(auth,"ADMINISTRATOR")) return true;

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) return false;

        if(meeting.getOrganizerId().equals(userId)) return true;

        return hasAnyEffectiveRole(userId,meeting,"RUKOVODILAC");
    }

    public boolean canRecordAttendance(Authentication auth, Long meetingId){
        Long userId = principalId(auth);
        if(hasRole(auth,"ADMINISTRATOR")) return true;

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) return false;

        if(meeting.getRecorderId().equals(userId) || meeting.getOrganizerId().equals(userId)) return true;

        return hasAnyEffectiveRole(userId,meeting, "ZAPISNICAR", "RUKOVODILAC");
    }

    private boolean hasRole(Authentication auth, String role){
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private Long principalId(Authentication auth) { return  ((AppUserPrincipal) auth.getPrincipal()).getId();}

    private boolean hasAnyEffectiveRole(Long userId, Meeting meeting, String... roles){
        var userInfo = authServiceClient.getUser(userId,meeting.getId(), meeting.getOrganizationalUnitId());
        for(String role : roles){
            if(userInfo.hasEffectiveRole(role)) return true;
        }
        return false;
    }
}
