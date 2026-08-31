package rs.vs.meetings_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {

    private Long recipientUserId;

    private String message;

    private String type;
}
