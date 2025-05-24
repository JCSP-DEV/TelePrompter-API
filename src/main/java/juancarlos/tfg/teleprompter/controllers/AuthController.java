package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.services.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller class that handles authentication-related operations including user registration,
 * login, session management, and logout functionality.
 *
 * @author Juan Carlos
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     *
     * @param user The user object containing registration details
     * @return ResponseEntity containing a success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        try {
            if (authService.register(user)) {
                return ResponseEntity.ok(Map.of("message", "User registered successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Username or Email already exists"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Authenticates a user and creates a new session.
     *
     * @param request The user object containing login credentials (username/email and password)
     * @param session The HTTP session to store user information
     * @return ResponseEntity containing login status and user information if successful
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User request, HttpSession session) {
        try {
            if ((request.getUsername() == null && request.getEmail() == null) || request.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Invalid request format"));
            }

            User loadedUser = null;
            if (request.getUsername() != null) {
                loadedUser = authService.loadUserByUsername(request.getUsername());
            } else {
                loadedUser = authService.loadUserByEmail(request.getEmail());
            }

            if (loadedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid credentials"));
            }

            if (!passwordEncoder.matches(request.getPassword(), loadedUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid credentials"));
            }

            if(!loadedUser.isVerified()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ User not verified"));
            }

            session.setAttribute("user", loadedUser.getUsername());
            loadedUser.setLastLoginDate(LocalDate.now());
            authService.updateUser(loadedUser);

            return ResponseEntity.ok(Map.of("message", "Login successful", "user", loadedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Invalid input"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "❌ Internal server error"));
        }
    }

    /**
     * Checks if there is an active session for the current user.
     *
     * @param session The HTTP session to check
     * @return ResponseEntity containing session status information
     */
    @GetMapping("/check-session")
    public ResponseEntity<Object> checkSession(HttpSession session) {
        try {
            String username = (String) session.getAttribute("user");
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No active session"));
            }
            return ResponseEntity.ok(Map.of("user", Map.of("active", username)));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "❌ Internal server error"));
        }
    }

    /**
     * Logs out the current user by invalidating their session.
     *
     * @param session The HTTP session to invalidate
     * @return ResponseEntity containing logout status message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        System.out.println("Logout");
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No active session to log out"));
        }
        session.invalidate();
        System.out.println("Session invalidated");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}