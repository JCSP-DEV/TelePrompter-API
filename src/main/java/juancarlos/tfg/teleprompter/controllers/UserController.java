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

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final Utils utils;

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


    @PostMapping("/delete{id}")
    public ResponseEntity<Map<String, String>> deleteUser(HttpSession session, @PathVariable("id") Long id) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        User currentUser = userService.loadUserByUsername(session.getAttribute("user").toString());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ Invalid session user"));
        }

        User targetUser = userService.loadUserById(id);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN.toString());
        boolean isSelf = currentUser.getId().equals(targetUser.getId());

        if (isAdmin || isSelf) {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User successfully deleted"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No enough permissions"));
    }

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

        if (userService.updateUser(user)) {
            return ResponseEntity.ok(Map.of("message", "User updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Failed to update user"));
        }
    }

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

        if (userService.updateUserById(id, user)) {
            return ResponseEntity.ok(Map.of("message", "User updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Failed to update user"));
        }
    }
}