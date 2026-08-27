package rs.vs.export_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.vs.export_service.dto.MeetingReportDto;
import rs.vs.export_service.service.ExportService;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestBody MeetingReportDto report, @RequestHeader(value = "X-Internal-Api-Key",required = false) String apiKey){
        if(!authorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(exportService.toPdf(report));
    }

    @PostMapping("/xlsx")
    public  ResponseEntity<byte[]> xslx(@RequestBody MeetingReportDto report, @RequestHeader(value = "X-Internal-Api-Key",required = false) String apiKey){
        if(!authorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(exportService.toXslx(report));
    }

    private boolean authorized(String apiKey){ return internalApiKey.equals(apiKey);}
}
