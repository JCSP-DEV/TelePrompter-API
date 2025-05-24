package juancarlos.tfg.teleprompter.utils;

import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides common helper methods for user authentication and validation.
 * Contains methods for checking user session status and user existence.
 *
 * @author Juan Carlos
 */
@Component
public class Utils {

    private final UserRepository userRepository;

    /**
     * Constructs a new Utils instance with the specified UserRepository.
     *
     * @param userRepository The repository for user operations
     */
    public Utils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks if a user is not logged in by verifying the session attribute.
     *
     * @param session The HTTP session to check
     * @return true if no user is logged in, false otherwise
     */
    public boolean isNotLogged(HttpSession session) {
        return session.getAttribute("user") == null;
    }

    /**
     * Checks if a user already exists in the system by username or email.
     *
     * @param user The user to check for existence
     * @return true if the user exists (by username or email), false otherwise
     */
    public boolean userExists(User user) {
        return userRepository.findByUsername(user.getUsername()).isPresent() ||
               userRepository.findByEmail(user.getEmail()).isPresent();
    }
}




