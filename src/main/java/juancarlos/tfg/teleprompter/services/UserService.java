package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Utils utils;

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
        userRepository.deleteById(id);
    }

    public User loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
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

    public String resetPassword(String username, String email, String token, String password) {
        Optional<User> user = userRepository.findByEmail(email).or(() -> userRepository.findByUsername(username));

        if (user.isEmpty()) {
            return "Usuario no encontrado.";
        }

        if (user.get().getToken() == null || !user.get().getToken().equals(token)) {
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
}