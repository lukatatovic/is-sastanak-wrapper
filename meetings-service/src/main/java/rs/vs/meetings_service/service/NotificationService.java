package rs.vs.meetings_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.vs.meetings_service.dto.NotificationDto;
import rs.vs.meetings_service.exception.ResourceNotFoundReception;
import rs.vs.meetings_service.model.Notification;
import rs.vs.meetings_service.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(Long recipientUserId, String message, String type, Long meetingId) {
        if (recipientUserId == null) return;
        notificationRepository.save(Notification.builder()
                .recipientUserId(recipientUserId)
                .message(message)
                .type(type)
                .meetingId(meetingId)
                .read(false)
                .build());
    }

    @Transactional
    public void notifyMany(List<Long> recipientUserIds, String message, String type, Long meetingId) {
        recipientUserIds.stream().distinct().forEach(id -> notify(id, message, type, meetingId));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findUnread(Long userId) {
        return notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.getType(), n.getMeetingId(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long requestingUserId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundReception("Obavestenje ne postoji"));

        if (!n.getRecipientUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Ovo obavestenje ne pripada vama");
        }
        n.setRead(true);
    }
}
