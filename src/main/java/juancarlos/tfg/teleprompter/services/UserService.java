package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import juancarlos.tfg.teleprompter.repositories.PrompterResposirtoy;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Utils utils;
    private final PrompterResposirtoy prompterRepository;

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
        user.setToken(token); // Set token in the input user object
        userRepository.save(newUser);
        return true;
    }


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

    public void requestPasswordReset(User user) {
        String token = String.format("%06d", new Random().nextInt(1000000));
        mailService.sendPasswordResetEmail(user.getEmail(), token);
        user.setToken(token);
        user.setVerified(false);
        user.setUpdatedDate(LocalDate.now());
        user.setTokenExpiryDate(LocalDate.now().plusDays(1));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = loadUserById(id);
        if (user == null) {
            return;
        }

        log.info("Starting deletion process for user: {}", user.getUsername());

        // Delete user's upload directory
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

        // Delete all teleprompter records associated with the user
        List<Teleprompter> userPrompters = prompterRepository.findByUser(user);
        log.info("Found {} teleprompters to delete for user: {}", userPrompters.size(), user.getUsername());
        if (!userPrompters.isEmpty()) {
            prompterRepository.deleteAll(userPrompters);
            log.info("Successfully deleted all teleprompters for user: {}", user.getUsername());
        }
        
        // Finally delete the user
        log.info("Deleting user record for: {}", user.getUsername());
        userRepository.deleteById(id);
        log.info("User deletion completed successfully for: {}", user.getUsername());
    }

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

    public User loadUserByUsername(String username) {
        List<User> users = userRepository.findAllByUsername(username);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean isUsernameTaken(String username, Long currentUserId) {
        List<User> users = userRepository.findAllByUsername(username);
        return users.stream()
                .anyMatch(user -> !user.getId().equals(currentUserId));
    }

    public boolean isEmailTaken(String email, Long currentUserId) {
        List<User> users = userRepository.findAllByEmail(email);
        return users.stream()
                .anyMatch(user -> !user.getId().equals(currentUserId));
    }

    public User loadUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    public User loadUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

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

    public boolean updateUser(User user) {
        return userRepository.findById(user.getId()).map(existingUser -> {
            updateUserFields(existingUser, user);
            userRepository.save(existingUser);
            return true;
        }).orElse(false);
    }

    public boolean updateUserById(Long id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            updateUserFields(existingUser, user);
            userRepository.save(existingUser);
            return true;
        }).orElse(false);
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}