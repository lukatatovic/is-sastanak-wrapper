package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionEntryDto {

    private String speakerName;

    private String content;

    private LocalDateTime timestamp;
}
