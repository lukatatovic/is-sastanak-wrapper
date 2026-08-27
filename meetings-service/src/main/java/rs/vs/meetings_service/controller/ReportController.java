package rs.vs.meetings_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.vs.meetings_service.dto.MeetingReportDto;
import rs.vs.meetings_service.service.ReportService;

@RestController
@RequestMapping("/api/meetings/{meetingId}/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("@meetingSecurityService.canViewMeeting(authentication, #meetingId)")
    @GetMapping("/short")
    public ResponseEntity<MeetingReportDto> shortReport(@PathVariable Long meetingId){
        return ResponseEntity.ok(reportService.shortReport(meetingId));
    }

    @PreAuthorize("@meetingSecurityService.canViewMeeting(authentication, #meetingId)")
    @GetMapping("/full")
    public ResponseEntity<MeetingReportDto> fullReport(@PathVariable Long meetingId){
        return ResponseEntity.ok(reportService.fullReport(meetingId));
    }
}
