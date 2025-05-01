package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileTranslatorService {


    private AiApiCallService aiApiCallService;

    public String translate(TextTranslationRequest request) {
        return aiApiCallService.translateText(request);
    }
}