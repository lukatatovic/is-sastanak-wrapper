package rs.vs.meetings_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.vs.meetings_service.model.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting,Long> {
}
