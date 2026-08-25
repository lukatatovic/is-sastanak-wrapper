package rs.vs.meetings_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgendaItemRequest {

    @NotBlank
    private String title;

    private String description;
}
