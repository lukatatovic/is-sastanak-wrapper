package auth.repository;

import auth.model.TemporaryRoleAssigment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemporaryRoleAssignmentRepository extends JpaRepository<TemporaryRoleAssigment,Long> {

    List<TemporaryRoleAssigment> findByUserId(Long userId);

    List<TemporaryRoleAssigment> findByUserIdAndRevokedFalse(Long userId);

    Optional<TemporaryRoleAssigment> findByUserIdAndMeetingIdAndRevokedFalse(Long userId, Long meetingId);

    List<TemporaryRoleAssigment> findByMeetingIdAndRevokedFalse(Long meetingId);
}
