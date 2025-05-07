package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import juancarlos.tfg.teleprompter.services.AiApiCallService;
import juancarlos.tfg.teleprompter.services.FileTranslatorService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/translator")
public class TranslatorController {

    private final AiApiCallService aiApiCallService;
    private final FileTranslatorService fileTranslatorService;
    private final Utils utils;

    @PostMapping("/text")
    public ResponseEntity<?> textTranslate(HttpSession session, @RequestBody TextTranslationRequest request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        TranslationResponse response = aiApiCallService.translateText(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/file")
    public ResponseEntity<?> fileTranslate(HttpSession session, @RequestBody TextTranslationRequest request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }
        
        TranslationResponse response = fileTranslatorService.translate(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }
}