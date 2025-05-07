package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileTranslatorService {

    private final AiApiCallService aiApiCallService;

    public TranslationResponse translate(TextTranslationRequest request) {
        return aiApiCallService.translateText(request);
    }
}