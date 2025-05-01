package juancarlos.tfg.teleprompter.utils;

import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    private final UserRepository userRepository;

    public Utils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isNotLogged(HttpSession session) {
        return session.getAttribute("user") == null;
    }

    public boolean userExists(User user) {
        return userRepository.findByUsername(user.getUsername()).isPresent() ||
               userRepository.findByEmail(user.getEmail()).isPresent();
    }
}