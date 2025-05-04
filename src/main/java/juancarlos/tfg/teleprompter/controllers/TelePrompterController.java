package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.TelePrompter;
import juancarlos.tfg.teleprompter.services.TelePrompterService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/teleprompter")
public class TelePrompterController {

    private final TelePrompterService telePrompterService;
    private final Utils utils;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTeleprompter(HttpSession session, @ModelAttribute TelePrompter telePrompter) {
        System.out.println("Creating teleprompter...");
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        System.out.println(telePrompter.toString());
        System.out.println("TelePrompter: " + telePrompter.getName());

        if (telePrompter.getName() == null || (telePrompter.getContent() == null && telePrompter.getFile() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Invalid request format"));
        }

        if (telePrompterService.create(telePrompter, (String) session.getAttribute("user"))) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Prompter created successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "❌ Prompter already exists"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getTeleprompters(HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        List<TelePrompter> prompters = telePrompterService.getPrompters((String) session.getAttribute("user"));
        if (prompters.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ No prompters found"));
        }

        return ResponseEntity.ok(prompters);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTeleprompterById(@PathVariable Long id, HttpSession session) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        TelePrompter telePrompter = telePrompterService.getPrompterById(id, (String) session.getAttribute("user"));
        if (telePrompter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Prompter not found"));
        }


        telePrompter.setUser(null);
        telePrompter.setFilePath(null);
        telePrompter.setUser(null);
        return ResponseEntity.ok(telePrompter);
    }
}
