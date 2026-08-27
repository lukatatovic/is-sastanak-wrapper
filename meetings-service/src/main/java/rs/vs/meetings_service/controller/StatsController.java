package rs.vs.meetings_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.vs.meetings_service.dto.ParticipationCountDto;
import rs.vs.meetings_service.security.AppUserPrincipal;
import rs.vs.meetings_service.service.StatsService;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/weekly")
    public ResponseEntity<ParticipationCountDto> weekly(Authentication auth){
        return ResponseEntity.ok(statsService.weekly(userId(auth)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ParticipationCountDto> monthly(Authentication auth){
        return ResponseEntity.ok(statsService.monthly(userId(auth)));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ParticipationCountDto> yearly(Authentication auth){
        return ResponseEntity.ok(statsService.yearly(userId(auth)));
    }


    private Long userId(Authentication auth) { return ((AppUserPrincipal) auth.getPrincipal()).getId();}
}
