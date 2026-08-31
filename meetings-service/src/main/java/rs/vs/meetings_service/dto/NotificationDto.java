package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationDto {

    private Long id;

    private String message;

    private String type;

    private Long meetingId;

    private LocalDateTime createdAt;
}
