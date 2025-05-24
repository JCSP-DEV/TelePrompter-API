package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.services.TelePrompterService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller class that handles teleprompter document operations.
 * Provides endpoints for creating, updating, retrieving, and deleting teleprompter documents.
 *
 * @author Juan Carlos
 */
@RestController
@AllArgsConstructor
@RequestMapping("/teleprompter")
public class TelePrompterController {

    private final TelePrompterService telePrompterService;
    private final Utils utils;

    /**
     * Creates a new teleprompter document with optional file upload.
     *
     * @param session The HTTP session to verify user authentication
     * @param telePrompter The teleprompter object containing document details and optional file
     * @return ResponseEntity containing success or error message
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTeleprompter(HttpSession session, @ModelAttribute Teleprompter telePrompter) {
        System.out.println("Creating teleprompter...");
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        if (telePrompter.getName() == null || (telePrompter.getContent() == null && telePrompter.getFile() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Invalid request format"));
        }

        if (telePrompterService.create(telePrompter, (String) session.getAttribute("user"))) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Prompter created successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Prompter already exists"));
        }
    }

    /**
     * Updates an existing teleprompter document.
     * @param id The ID of the teleprompter to update
     * @param telePrompter The teleprompter object containing updated information
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing success or error message
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTeleprompter(@PathVariable Long id, @RequestBody Teleprompter telePrompter, HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        if (telePrompterService.update(id, telePrompter, (String) session.getAttribute("user"))) {
            return ResponseEntity.ok(Map.of("message", "Prompter updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Prompter not found"));
        }
    }

    /**
     * Retrieves all teleprompter documents for the current user.
     *
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing a list of teleprompter documents or error message
     */
    @GetMapping
    public ResponseEntity<?> getTeleprompters(HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        List<Teleprompter> prompters = telePrompterService.getPrompters((String) session.getAttribute("user"));
        if (prompters.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ No prompters found"));
        }

        return ResponseEntity.ok(prompters);
    }

    /**
     * Retrieves a specific teleprompter document by ID.
     *
     * @param id The ID of the teleprompter to retrieve
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing the teleprompter document or error message
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeleprompterById(@PathVariable Long id, HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        Teleprompter telePrompter = telePrompterService.getPrompterById(id, (String) session.getAttribute("user"));
        if (telePrompter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Prompter not found"));
        }

        telePrompter.setUser(null);
        telePrompter.setFilePath(null);
        telePrompter.setUser(null);
        return ResponseEntity.ok(telePrompter);
    }

    /**
     * Downloads the file associated with a teleprompter document.
     *
     * @param id The ID of the teleprompter document
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing the file for download or error message
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadTeleprompterFile(@PathVariable Long id, HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        Teleprompter telePrompter = telePrompterService.getPrompterById(id, (String) session.getAttribute("user"));
        if (telePrompter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Prompter not found"));
        }

        return telePrompterService.downloadFile(telePrompter);
    }

    /**
     * Deletes a teleprompter document.
     *
     * @param id The ID of the teleprompter to delete
     * @param session The HTTP session to verify user authentication
     * @return ResponseEntity containing success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeleprompter(@PathVariable Long id, HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        if (telePrompterService.delete(id, (String) session.getAttribute("user"))) {
            return ResponseEntity.ok(Map.of("message", "Prompter deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Prompter not found"));
        }
    }
}
