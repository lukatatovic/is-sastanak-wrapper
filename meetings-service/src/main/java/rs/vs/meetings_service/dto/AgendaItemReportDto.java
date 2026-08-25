package rs.vs.meetings_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendaItemReportDto {

    private Integer orderNum;
    private String title;
    private String description;
    private List<DiscussionEntryDto> discussionEntries;
    private String conclusion;
}
