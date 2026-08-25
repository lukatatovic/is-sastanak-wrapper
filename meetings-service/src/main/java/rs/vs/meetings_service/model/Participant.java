package rs.vs.meetings_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    private Long userId;

    private String externalFirstName;

    private String externalLastName;

    private String externalOrganizationalUnit;

    private String externalJobTitle;

    private String externalCountry;

    @Builder.Default
    private boolean plannedToAttend = true;

    @Builder.Default
    private boolean actuallyAttended = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_for_participant_id")
    private Participant replacementFor;
}
