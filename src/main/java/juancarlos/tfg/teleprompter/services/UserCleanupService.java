package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service class that handles cleanup operations for user accounts.
 * Provides scheduled tasks for removing unverified users.
 *
 * @author Juan Carlos
 */
@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    /**
     * Constructs a new UserCleanupService with the specified UserRepository.
     *
     * @author Juan Carlos
     * @param userRepository The repository for user operations
     */
    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Scheduled task that runs daily at midnight to delete unverified users
     * whose verification tokens have expired.
     *
     * @author Juan Carlos
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteUnverifiedUsers() {
        userRepository.findAll().stream().filter(user ->
                !user.isVerified() && user.getTokenExpiryDate() != null &&
                        user.getTokenExpiryDate().isBefore(LocalDate.now())).forEach(userRepository::delete);
    }
}