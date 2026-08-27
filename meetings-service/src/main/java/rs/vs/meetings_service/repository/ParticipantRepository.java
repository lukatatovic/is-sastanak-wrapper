package rs.vs.meetings_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.vs.meetings_service.model.Participant;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant,Long> {

    List<Participant> findByMeetingId(Long meetingId);
}
