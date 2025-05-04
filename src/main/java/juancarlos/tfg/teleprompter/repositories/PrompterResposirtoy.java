package juancarlos.tfg.teleprompter.repositories;


import juancarlos.tfg.teleprompter.models.TelePrompter;
import juancarlos.tfg.teleprompter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrompterResposirtoy extends JpaRepository<TelePrompter, Long> {

    Optional<TelePrompter> findByNameAndUserId(String name, Long user_id);

    List<TelePrompter> findByUser(User user);

    Optional<TelePrompter> findByIdAndUser(Long id, User user);
}
