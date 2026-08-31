package rs.vs.meetings_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.vs.meetings_service.dto.NotificationDto;
import rs.vs.meetings_service.dto.NotificationRequest;
import rs.vs.meetings_service.security.AppUserPrincipal;
import rs.vs.meetings_service.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> unread(Authentication auth) {
        Long userId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        return ResponseEntity.ok(notificationService.findUnread(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal")
    public ResponseEntity<Void> createFromOtherService(
            @RequestBody NotificationRequest request,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.notify(request.getRecipientUserId(), request.getMessage(), request.getType(), null);
        return ResponseEntity.noContent().build();
    }
}
