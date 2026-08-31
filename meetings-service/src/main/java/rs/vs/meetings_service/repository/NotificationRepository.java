package rs.vs.meetings_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.vs.meetings_service.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(Long recipientUserId);
}
