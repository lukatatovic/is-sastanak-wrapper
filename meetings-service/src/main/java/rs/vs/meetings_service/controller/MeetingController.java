package rs.vs.meetings_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.vs.meetings_service.dto.MeetingCreateRequest;
import rs.vs.meetings_service.dto.MeetingDetailDto;
import rs.vs.meetings_service.model.Meeting;
import rs.vs.meetings_service.security.AppUserPrincipal;
import rs.vs.meetings_service.service.MeetingService;

import java.awt.print.Pageable;

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
}
