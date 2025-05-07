package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiApiCallService {
    private static final Logger logger = LoggerFactory.getLogger(AiApiCallService.class);
    private static final String MODEL = "deepseek/deepseek-r1:free";
    private static final String BASE_URL = "https://openrouter.ai/api/v1";
    
    @Value("${ai.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AiApiCallService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    private HttpEntity<String> createRequestEntity(TextTranslationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        String prompt = String.format(
            "Translate the following text into %s. Provide the response in JSON format with the following structure: " +
            "{\"translated_text\": \"translation\", \"original_language\": \"detected language\", \"target_language\": \"%s\"}. " +
            "Ensure the translation is accurate and maintains the original meaning. " +
            "Text to translate: %s",
            request.getTargetLanguage(),
            request.getTargetLanguage(),
            request.getText()
        );
        
        String body = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}",
            MODEL,
            prompt
        );
        
        return new HttpEntity<>(body, headers);
    }
    
    public TranslationResponse translateText(TextTranslationRequest request) {
        TranslationResponse response = new TranslationResponse();
        response.setTargetLanguage(request.getTargetLanguage());
        
        try {
            HttpEntity<String> entity = createRequestEntity(request);
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                BASE_URL + "/chat/completions",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
                JsonNode root = objectMapper.readTree(apiResponse.getBody());
                JsonNode messageNode = root.path("choices").get(0).path("message");
                String content = messageNode.path("content").asText();
                
                // Parse the JSON response from the AI
                JsonNode translationNode = objectMapper.readTree(content);
                response.setTranslatedText(translationNode.path("translated_text").asText());
                response.setOriginalLanguage(translationNode.path("original_language").asText());
                response.setSuccess(true);
            } else {
                handleError(response, "API returned non-OK status: " + apiResponse.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Translation failed", e);
            handleError(response, "Translation failed: " + e.getMessage());
        }
        
        return response;
    }
    
    private void handleError(TranslationResponse response, String errorMessage) {
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setTranslatedText("");
        response.setOriginalLanguage("");
    }
}