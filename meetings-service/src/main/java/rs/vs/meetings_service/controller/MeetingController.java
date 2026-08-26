package rs.vs.meetings_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.vs.meetings_service.dto.MeetingCreateRequest;
import rs.vs.meetings_service.dto.MeetingDetailDto;
import rs.vs.meetings_service.dto.MeetingSummaryDto;
import rs.vs.meetings_service.model.Meeting;
import rs.vs.meetings_service.security.AppUserPrincipal;
import rs.vs.meetings_service.service.MeetingService;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PreAuthorize("hasAnyRole('RUKOVODILAC','ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<MeetingDetailDto> create(Authentication auth, @Valid @RequestBody MeetingCreateRequest request){
        Long organizerId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        Meeting meeting = meetingService.createMeeting(organizerId,request);
        return ResponseEntity.ok(meetingService.toDetailDto(meeting));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<MeetingSummaryDto>> myMeetings(Authentication auth, Pageable pageable){
        Long userId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        return ResponseEntity.ok(meetingService.findVisibleToUser(userId,pageable));
    }

    @GetMapping
    public ResponseEntity<Page<MeetingSummaryDto>> byOroUnit(@RequestParam Long orgUnitId, Pageable pageable){
        return ResponseEntity.ok(meetingService.findByOrganizationalUnit(orgUnitId,pageable));
    }
}
