package juancarlos.tfg.teleprompter.repositories;


import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrompterRepository extends JpaRepository<Teleprompter, Long> {

    Optional<Teleprompter> findByNameAndUserId(String name, Long user_id);

    List<Teleprompter> findByUser(User user);

    Optional<Teleprompter> findByIdAndUser(Long id, User user);
}
