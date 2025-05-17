package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteUnverifiedUsers() {
        userRepository.findAll().stream().filter(user -> !user.isVerified() && user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isBefore(LocalDate.now())).forEach(userRepository::delete);
    }
}