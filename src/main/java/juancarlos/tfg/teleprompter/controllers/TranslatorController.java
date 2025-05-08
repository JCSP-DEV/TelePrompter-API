package juancarlos.tfg.teleprompter.controllers;

import juancarlos.tfg.teleprompter.utils.Utils;
import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import juancarlos.tfg.teleprompter.models.FileTranslationRequest;
import juancarlos.tfg.teleprompter.services.AiApiCallService;
import juancarlos.tfg.teleprompter.services.FileTranslatorService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

        TranslationResponse result = aiApiCallService.translateText(request);
        if (result.getError() == null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> fileTranslate(HttpSession session, @ModelAttribute FileTranslationRequest request) {
        System.out.println("Translating file...");
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        try {
            TranslationResponse result = fileTranslatorService.translateFile(request, (String) session.getAttribute("user"));
            if (result.getError() == null) {
                System.out.println("File translated successfully");
                return ResponseEntity.ok(result);
            } else {
                System.out.println("Error translating file: " + result.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "❌ Error processing file: " + e.getMessage()));
        }
    }
}