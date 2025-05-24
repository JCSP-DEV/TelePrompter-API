package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import juancarlos.tfg.teleprompter.repositories.PrompterRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service class that handles user-related operations.
 * Provides functionality for user management, authentication, and profile updates.
 *
 * @author Juan Carlos
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Utils utils;
    private final PrompterRepository prompterRepository;

    /**
     * Creates a new user in the system.
     * Sends a verification email to the user's email address.
     *
     * @param user The user object containing user details
     * @return true if the user was created successfully, false otherwise
     */
    public boolean createUser(User user) {
        if (utils.userExists(user)) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(user.getRole());
        newUser.setCreatedDate(LocalDate.now());
        newUser.setTokenExpiryDate(LocalDate.now().minusDays(1));
        newUser.setVerified(false);
        String token = String.format("%06d", new Random().nextInt(1000000));
        mailService.sendVerificationEmail(newUser.getEmail(), token);
        newUser.setToken(token);
        user.setToken(token);
        userRepository.save(newUser);
        return true;
    }

    /**
     * Activates a user account using the verification token.
     *
     * @param token The verification token sent to the user's email
     * @return A message indicating the result of the activation attempt
     */
    public String activateUser(String token) {
        Optional<User> user = userRepository.findByToken(token);

        if (user.isEmpty()) {
            return "Invalid verification code";
        }

        if (user.get().isVerified()) {
            return "The user is already verified";
        }

        if (user.get().getToken().equals(token)) {
            user.get().setVerified(true);
            user.get().setToken(null);
            user.get().setTokenExpiryDate(null);
            userRepository.save(user.get());
            return "Account activated successfully";
        }

        return "Invalid verification code";
    }

    /**
     * Initiates the password reset process for a user.
     * Sends a password reset email with a reset token.
     *
     * @param user The user requesting the password reset
     */
    public void requestPasswordReset(User user) {
        String token = String.format("%06d", new Random().nextInt(1000000));
        mailService.sendPasswordResetEmail(user.getEmail(), token);
        user.setToken(token);
        user.setVerified(false);
        user.setUpdatedDate(LocalDate.now());
        user.setTokenExpiryDate(LocalDate.now().plusDays(1));
        userRepository.save(user);
    }

    /**
     * Deletes a user and all associated data from the system.
     * This includes user files, teleprompter documents, and the user record.
     *
     * @param id The ID of the user to delete
     */
    public void deleteUser(Long id) {
        User user = loadUserById(id);
        if (user == null) {
            return;
        }

        log.info("Starting deletion process for user: {}", user.getUsername());

        try {
            File userDir = new File("uploads/" + user.getUsername());
            if (userDir.exists()) {
                log.info("Deleting user directory and all contents: {}", userDir.getAbsolutePath());
                deleteDirectory(userDir);
                if (userDir.exists()) {
                    log.warn("User directory still exists after deletion attempt: {}", userDir.getAbsolutePath());
                } else {
                    log.info("User directory successfully deleted: {}", userDir.getAbsolutePath());
                }
            } else {
                log.info("User directory does not exist: {}", userDir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error deleting user directory for user: " + user.getUsername(), e);
        }

        List<Teleprompter> userPrompters = prompterRepository.findByUser(user);
        log.info("Found {} teleprompters to delete for user: {}", userPrompters.size(), user.getUsername());
        if (!userPrompters.isEmpty()) {
            prompterRepository.deleteAll(userPrompters);
            log.info("Successfully deleted all teleprompters for user: {}", user.getUsername());
        }
        
        log.info("Deleting user record for: {}", user.getUsername());
        userRepository.deleteById(id);
        log.info("User deletion completed successfully for: {}", user.getUsername());
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory The directory to delete
     */
    private void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }

        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        
        if (!directory.delete()) {
            log.warn("Failed to delete directory: {}", directory.getAbsolutePath());
        }
    }

    /**
     * Loads a user by their username.
     *
     * @param username The username to search for
     * @return The user object if found, null otherwise
     */
    public User loadUserByUsername(String username) {
        List<User> users = userRepository.findAllByUsername(username);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Checks if a username is already taken by another user.
     *
     * @param username The username to check
     * @param currentUserId The ID of the current user (to exclude from the check)
     * @return true if the username is taken, false otherwise
     */
    public boolean isUsernameTaken(String username, Long currentUserId) {
        List<User> users = userRepository.findAllByUsername(username);
        return users.stream()
                .anyMatch(user -> !user.getId().equals(currentUserId));
    }

    /**
     * Checks if an email is already taken by another user.
     *
     * @param email The email to check
     * @param currentUserId The ID of the current user (to exclude from the check)
     * @return true if the email is taken, false otherwise
     */
    public boolean isEmailTaken(String email, Long currentUserId) {
        List<User> users = userRepository.findAllByEmail(email);
        return users.stream()
                .anyMatch(user -> !user.getId().equals(currentUserId));
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
     * Loads a user by their ID.
     *
     * @param id The ID of the user to load
     * @return The user object if found, null otherwise
     */
    public User loadUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    /**
     * Loads all users in the system.
     *
     * @return A list of all users
     */
    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Resets a user's password using a reset token or current session.
     *
     * @param username The username of the user
     * @param email The email of the user
     * @param token The reset token
     * @param password The new password
     * @param isLogged Whether the user is currently logged in
     * @return A message indicating the result of the password reset
     */
    public String resetPassword(String username, String email, String token, String password, boolean isLogged) {
        Optional<User> user = userRepository.findByEmail(email).or(() -> userRepository.findByUsername(username));

        if (user.isEmpty()) {
            return "Usuario no encontrado.";
        }

        if (!isLogged && (user.get().getToken() == null || !user.get().getToken().equals(token))) {
            return "Invalid reset token";
        }

        user.get().setPassword(passwordEncoder.encode(password));
        user.get().setToken(null);
        user.get().setVerified(true);
        userRepository.save(user.get());
        return "Password reset successful";
    }

    /**
     * Updates the fields of an existing user with new values.
     *
     * @param existingUser The existing user object to update
     * @param newUser The user object containing new values
     */
    private void updateUserFields(User existingUser, User newUser) {
        if (newUser.getUsername() != null) {
            existingUser.setUsername(newUser.getUsername());
        }
        if (newUser.getEmail() != null) {
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        if (newUser.getRole() != null) {
            existingUser.setRole(newUser.getRole());
        }
        existingUser.setUpdatedDate(LocalDate.now());
    }

    /**
     * Updates the current user's profile information.
     *
     * @param user The user object containing updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        return userRepository.findById(user.getId()).map(existingUser -> {
            updateUserFields(existingUser, user);
            userRepository.save(existingUser);
            return true;
        }).orElse(false);
    }

    /**
     * Updates a user's profile information by ID.
     *
     * @param id The ID of the user to update
     * @param user The user object containing updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserById(Long id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            updateUserFields(existingUser, user);
            userRepository.save(existingUser);
            return true;
        }).orElse(false);
    }

    /**
     * Verifies if a provided password matches the user's stored password.
     *
     * @param user The user to verify the password for
     * @param rawPassword The password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}