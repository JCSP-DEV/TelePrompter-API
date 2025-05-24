package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.enums.UserRole;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

/**
 * Service class that handles authentication-related operations.
 * Provides functionality for user registration, verification, and authentication.
 *
 * @author Juan Carlos
 */
@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Utils utils;

    /**
     * Registers a new user in the system.
     * Creates a new user account and sends a verification email.
     *
     * @param user The user object containing registration details
     * @return true if registration was successful, false otherwise
     */
    public boolean register(User user) {
        if (utils.userExists(user)) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(UserRole.USER.toString());
        newUser.setCreatedDate(LocalDate.now());
        String token = String.format("%06d", new Random().nextInt(1000000));
        mailService.sendVerificationEmail(newUser.getEmail(), token);
        newUser.setToken(token);
        newUser.setVerified(false);
        newUser.setTokenExpiryDate(LocalDate.now().plusDays(1));
        user.setToken(token);
        userRepository.save(newUser);
        return true;
    }

    /**
     * Loads a user by their username.
     *
     * @param name The username to search for
     * @return The user object if found, null otherwise
     */
    public User loadUserByUsername(String name) {
        Optional<User> user = userRepository.findByUsername(name);
        return user.orElse(null);
    }

    /**
     * Loads a user by their email address.
     *
     * @param email The email address to search for
     * @return The user object if found, null otherwise
     */
    public User loadUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    /**
     * Updates a user's information in the system.
     *
     * @param user The user object containing updated information
     */
    public void updateUser(User user) {
        userRepository.save(user);
    }
}