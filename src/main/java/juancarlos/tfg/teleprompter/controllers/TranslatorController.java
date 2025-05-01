package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
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


        String result = aiApiCallService.translateText(request);
        if (result != null) {
            System.out.println(result);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body("❌ Translation failed");
        }
    }

    @PostMapping("/file")
    public ResponseEntity<?> fileTranslate(HttpSession session, @RequestBody TextTranslationRequest request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }
        System.out.println(request.getText());
        String result = fileTranslatorService.translate(request);
        if (result != null) {
            System.out.println(result);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body("❌ Translation failed");
        }
    }
}