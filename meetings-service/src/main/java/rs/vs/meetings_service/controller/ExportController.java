package rs.vs.meetings_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.vs.meetings_service.client.ExportServiceClient;
import rs.vs.meetings_service.dto.MeetingReportDto;
import rs.vs.meetings_service.service.ReportService;

@RestController
@RequestMapping("/api/meetings/{meetingId}/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportServiceClient exportServiceClient;
    private final ReportService reportService;

    @PreAuthorize("@meetingSecurityService.canViewMeeting(authentication, #meetingId)")
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long meetingId, @RequestParam(defaultValue = "false") boolean full){
        MeetingReportDto report = full ? reportService.fullReport(meetingId) : reportService.shortReport(meetingId);
        byte[] content = exportServiceClient.exportPdf(report);
        return fileResponse(content, "izvesaj-sastanak-" + meetingId + ".pdf",MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("@meetingSecurityService.canViewMeeting(authentication, #meetingId)")
    @GetMapping("/xlsx")
    public ResponseEntity<byte[]> exportXslx(@PathVariable Long meetingId, @RequestParam(defaultValue = "false") boolean full){
        MeetingReportDto report = full ? reportService.fullReport(meetingId) : reportService.shortReport(meetingId);
        byte[] content = exportServiceClient.exportXslx(report);
        return fileResponse(content, "izvesaj-sastanak-" + meetingId + ".xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @PreAuthorize("@meetingSecurityService.canViewMeeting(authentication, #meetingId)")
    @GetMapping("/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable Long meetingId, @RequestParam(defaultValue = "false") boolean full){
        MeetingReportDto report = full ? reportService.fullReport(meetingId) : reportService.shortReport(meetingId);
        byte[] content = exportServiceClient.exportDocx(report);
        return fileResponse(content, "izvesaj-sastanak-" + meetingId + ".docx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] content, String fileName, MediaType mediaType){
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
