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

/**
 * Controller class that handles translation operations for both text and files.
 * Provides endpoints for translating text content and file contents using AI services.
 *
 * @author Juan Carlos
 */
@RestController
@AllArgsConstructor
@RequestMapping("/translator")
public class TranslatorController {

    private final AiApiCallService aiApiCallService;
    private final FileTranslatorService fileTranslatorService;
    private final Utils utils;

    /**
     * Translates text content to the specified target language.
     *
     * @param session The HTTP session to verify user authentication
     * @param request The translation request containing text and target language
     * @return ResponseEntity containing the translation result or error message
     */
    @PostMapping("/text")
    public ResponseEntity<?> textTranslate(HttpSession session, @RequestBody TextTranslationRequest request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        System.out.println("Translating text...");
        TranslationResponse result = aiApiCallService.translateText(request);
        if (result.getError() == null) {
            System.out.println("Text translated successfully");
            return ResponseEntity.ok(result);
        } else {
            System.out.println("Error translating text: " + result.getError());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Translates the contents of a file to the specified target language.
     *
     * @param session The HTTP session to verify user authentication
     * @param request The translation request containing file and target language
     * @return ResponseEntity containing the translation result or error message
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> fileTranslate(HttpSession session, @ModelAttribute FileTranslationRequest request) {
        if (utils.isNotLogged(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ No active session"));
        }

        try {
            System.out.println("Translating file...");
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