package juancarlos.tfg.teleprompter.repositories;

import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Teleprompter entities.
 * Provides methods for querying and managing teleprompter documents in the database.
 *
 * @author Juan Carlos
 */
public interface PrompterRepository extends JpaRepository<Teleprompter, Long> {

    /**
     * Finds a teleprompter document by its name and user ID.
     *
     * @author Juan Carlos
     * @param name The name of the teleprompter document
     * @param user_id The ID of the user who owns the document
     * @return An Optional containing the teleprompter if found
     */
    Optional<Teleprompter> findByNameAndUserId(String name, Long user_id);

    /**
     * Finds all teleprompter documents owned by a specific user.
     *
     * @author Juan Carlos
     * @param user The user who owns the documents
     * @return A list of teleprompter documents owned by the user
     */
    List<Teleprompter> findByUser(User user);

    /**
     * Finds a teleprompter document by its ID and owner user.
     *
     * @author Juan Carlos
     * @param id The ID of the teleprompter document
     * @param user The user who owns the document
     * @return An Optional containing the teleprompter if found
     */
    Optional<Teleprompter> findByIdAndUser(Long id, User user);
}
