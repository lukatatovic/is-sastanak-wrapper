package rs.vs.meetings_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.vs.meetings_service.model.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting,Long> {
    @Query("""
           select distinct m from Meeting m
           left join m.participants p
           where m.organizerId = :userId
              or m.recorderId = :userId
              or (p.userId = :userId)
           """)
    Page<Meeting> findAllVisibleToUser(@Param("userId") Long userId, Pageable pageable);
}
