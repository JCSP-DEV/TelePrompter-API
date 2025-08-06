package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.enums.UserRole;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller class that handles user-related operations including user management,
 * account activation, password reset, and user profile updates.
 *
 * @author Juan Carlos
 */
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final Utils utils;

    /**
     * Creates a new user in the system. Only accessible by administrators.
     *
     * @param session The HTTP session to verify user authentication
     * @param user The user object containing user details
     * @return ResponseEntity containing success or error message
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createUser(HttpSession session, @RequestBody User user) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        if (!userService.loadUserByUsername(session.getAttribute("user").toString()).getRole().equals(UserRole.ADMIN.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No enough permissions"));
        }

        if (userService.createUser(user)) {
            return ResponseEntity.ok(Map.of("message", "User created successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Username or Email already exists"));
        }
    }

    /**
     * Activates a user account using the verification token.
     *
     * @param request The user object containing the verification token
     * @return ResponseEntity containing activation status message
     */
    @PostMapping("/activate")
    public ResponseEntity<Object> activateAccount(@RequestBody User request) {
        if (request.getToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "No Token provided"));
        }

        String result = userService.activateUser(request.getToken());

        return switch (result) {
            case "Invalid verification code" ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", result));
            case "The user is already verified" ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", result));
            case "Account activated successfully" ->
                    ResponseEntity.status(HttpStatus.OK).body(Map.of("message", result));
            default -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", result));
        };
    }

    /**
     * Retrieves a user by their ID. Only accessible by administrators.
     *
     * @param session The HTTP session to verify user authentication
     * @param id The ID of the user to retrieve
     * @return ResponseEntity containing the user object or null if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(HttpSession session, @PathVariable("id") Long id) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if (!userService.loadUserByUsername(session.getAttribute("user").toString()).getRole().equals(UserRole.ADMIN.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = userService.loadUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves all users in the system. Only accessible by administrators.
     *
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing a list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        if (!userService.loadUserByUsername(session.getAttribute("user").toString()).getRole().equals(UserRole.ADMIN.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(userService.loadAllUsers());
    }

    /**
     * Initiates the password reset process by sending a reset email.
     *
     * @param request The user object containing username or email
     * @return ResponseEntity containing the status of the password reset request
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody User request) {
        if ((request.getUsername() == null && request.getEmail() == null)) {
            System.out.println("Username or Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Username or Email is required"));
        }
        User loadedUser;
        if (request.getUsername() != null) {
            loadedUser = userService.loadUserByUsername(request.getUsername());
        } else {
            loadedUser = userService.loadUserByEmail(request.getEmail());
        }

        if (loadedUser == null) {
            System.out.println("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ User not found"));
        }

        userService.requestPasswordReset(loadedUser);

        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    /**
     * Resets a user's password using a reset token or current session.
     *
     * @param request The user object containing reset token and new password
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing the status of the password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody User request, HttpSession session) {
        boolean isLogged = !utils.isNotLogged(session);
        if(isLogged) {
            request.setUsername(session.getAttribute("user").toString());
        }
        if (request.getToken() == null && !isLogged) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ No Token provided"));
        }
        if ((request.getEmail() == null && request.getUsername() == null) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Email or Username required"));
        }
        if (request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Password required"));
        }

        String result = userService.resetPassword(request.getUsername(), request.getEmail(), request.getToken(), request.getPassword(), isLogged);

        return switch (result) {
            case "Usuario no encontrado." ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", result));
            case "Invalid reset token" ->
                    ResponseEntity.status(HttpStatus.OK).body(Map.of("message", result));
            default -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", result));
        };
    }

    /**
     * Deletes a user from the system. Can be performed by administrators or the user themselves.
     *
     * @param session The HTTP session to verify user authentication
     * @param id The ID of the user to delete
     * @param request The user object containing password for self-deletion
     * @return ResponseEntity containing the status of the deletion operation
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(HttpSession session, @PathVariable("id") Long id, @RequestBody(required = false) User request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        User currentUser = userService.loadUserByUsername(session.getAttribute("user").toString());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid session user"));
        }

        if(id == -1) {
            if (request == null || request.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Password required for self-deletion"));
            }
            if (!userService.verifyPassword(currentUser, request.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid password"));
            }
            id = currentUser.getId();
        }

        User targetUser = userService.loadUserById(id);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ User not found"));
        }

        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN.toString());
        boolean isSelf = currentUser.getId().equals(targetUser.getId());

        if (isAdmin || isSelf) {
            try {
                userService.deleteUser(id);
                return ResponseEntity.ok(Map.of("message", "✅ User successfully deleted"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Error deleting user: " + e.getMessage()));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No enough permissions"));
    }

    /**
     * Updates the current user's profile information.
     *
     * @param session The HTTP session to verify user authentication
     * @param user The user object containing updated information
     * @return ResponseEntity containing the status of the update operation
     */
    @PatchMapping("/update")
    public ResponseEntity<Map<String, String>> updateUser(HttpSession session, @RequestBody User user) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        User currentUser = userService.loadUserByUsername(session.getAttribute("user").toString());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid session user"));
        }
        user.setId(currentUser.getId());

        // Check for duplicate username
        if (user.getUsername() != null && !user.getUsername().equals(currentUser.getUsername())) {
            if (userService.isUsernameTaken(user.getUsername(), currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Username already exists"));
            }
        }

        // Check for duplicate email
        if (user.getEmail() != null && !user.getEmail().equals(currentUser.getEmail())) {
            if (userService.isEmailTaken(user.getEmail(), currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Email already exists"));
            }
        }

        if (userService.updateUser(user)) {
            return ResponseEntity.ok(Map.of("message", "✅ User updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Failed to update user"));
        }
    }

    /**
     * Updates a user's profile information by ID. Only accessible by administrators.
     *
     * @param session The HTTP session to verify user authentication
     * @param id The ID of the user to update
     * @param user The user object containing updated information
     * @return ResponseEntity containing the status of the update operation
     */
    @PatchMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateUserById(HttpSession session, @PathVariable("id") Long id, @RequestBody User user) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        User currentUser = userService.loadUserByUsername(session.getAttribute("user").toString());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid session user"));
        }

        if (!currentUser.getRole().equals(UserRole.ADMIN.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No enough permissions"));
        }

        User targetUser = userService.loadUserById(id);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ User not found"));
        }

        if (user.getUsername() != null && !user.getUsername().equals(targetUser.getUsername())) {
            if (userService.isUsernameTaken(user.getUsername(), targetUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Username already exists"));
            }
        }

        if (user.getEmail() != null && !user.getEmail().equals(targetUser.getEmail())) {
            if (userService.isEmailTaken(user.getEmail(), targetUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Email already exists"));
            }
        }

        if (userService.updateUserById(id, user)) {
            return ResponseEntity.ok(Map.of("message", "✅ User updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Failed to update user"));
        }
    }

}