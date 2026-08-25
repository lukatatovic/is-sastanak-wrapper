package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgendaItemDetailDto {

    private Long id;

    private Integer orderNum;

    private String title;

    private String description;

    private String conclusion;
}
