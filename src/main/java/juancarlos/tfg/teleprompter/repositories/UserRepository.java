package juancarlos.tfg.teleprompter.repositories;

import juancarlos.tfg.teleprompter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * Provides methods for querying and managing user accounts in the database.
 *
 * @author Juan Carlos
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their username.
     *
     * @author Juan Carlos
     * @param username The username to search for
     * @return An Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds all users with the specified username.
     *
     * @author Juan Carlos
     * @param username The username to search for
     * @return A list of users with the specified username
     */
    List<User> findAllByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @author Juan Carlos
     * @param email The email address to search for
     * @return An Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds all users with the specified email address.
     *
     * @author Juan Carlos
     * @param email The email address to search for
     * @return A list of users with the specified email address
     */
    List<User> findAllByEmail(String email);

    /**
     * Finds a user by their verification or reset token.
     *
     * @author Juan Carlos
     * @param token The token to search for
     * @return An Optional containing the user if found
     */
    Optional<User> findByToken(String token);
}