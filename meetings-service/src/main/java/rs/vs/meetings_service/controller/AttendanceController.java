package rs.vs.meetings_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.vs.meetings_service.dto.AttendanceUpdateRequest;
import rs.vs.meetings_service.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/meetings/{meetingId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final MeetingService meetingService;

    @PreAuthorize("@meetingSecurityService.canRecordAttendance(authentication, #meetingId)")
    @PostMapping
    public ResponseEntity<Void> recordAttendance(@PathVariable Long meetingId, @RequestBody List<AttendanceUpdateRequest> updates){
        meetingService.recordAttendance(meetingId,updates);
        return ResponseEntity.noContent().build();
    }
}
