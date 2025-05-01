package juancarlos.tfg.teleprompter.repositories;

import juancarlos.tfg.teleprompter.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
