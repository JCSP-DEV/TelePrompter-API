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

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final Utils utils;

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
        user.setToken(token); // Set token in the input user object
        userRepository.save(newUser);
        return true;
    }

    public User loadUserByUsername(String name) {
        Optional<User> user = userRepository.findByUsername(name);
        return user.orElse(null);
    }

    public User loadUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }



}