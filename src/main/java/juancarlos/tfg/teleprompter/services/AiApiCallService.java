package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiApiCallService {

    @Value("${magicloops.api.url}")
    private String apiUrl;

    private static HttpEntity<String> httpBody(TextTranslationRequest request, HttpHeaders headers) {
        String body = String.format("{\"input\": \"Text to translate: '{%s}' {{(%s)}}\"}", 
            request.getText(), 
            request.getTargetLanguage());
        return new HttpEntity<>(body, headers);
    }

    public TranslationResponse translateText(TextTranslationRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = httpBody(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            String translatedText = root.path("text").asText();
            String originalLanguage = root.path("original_language").asText();
            
            return TranslationResponse.success(translatedText, originalLanguage, request.getTargetLanguage());
        } catch (Exception e) {
            e.printStackTrace();
            return TranslationResponse.error("Translation failed", e.getMessage());
        }
    }
}