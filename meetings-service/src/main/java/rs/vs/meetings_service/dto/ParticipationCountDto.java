package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipationCountDto {

    private String period;

    private long count;
}
