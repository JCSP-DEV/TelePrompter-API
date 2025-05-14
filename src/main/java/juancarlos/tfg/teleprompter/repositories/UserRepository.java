package juancarlos.tfg.teleprompter.repositories;

import juancarlos.tfg.teleprompter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUsername(String username);

    Optional<User> findByEmail(String email);
    List<User> findAllByEmail(String email);

    Optional<User> findByToken(String token);
}