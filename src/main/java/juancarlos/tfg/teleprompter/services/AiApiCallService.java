package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiApiCallService {

    private static final String API_KEY = "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";
    private static final String BASE_URL = "https://openrouter.ai/api/v1";

    private static HttpEntity<String> httpBody(TextTranslationRequest request, HttpHeaders headers) {
        String body = String.format("{\"model\": \"deepseek/deepseek-r1:free\", \"messages\": [{\"role\": \"user\", \"content\": \"Translate the following text into the language specified within brackets and parentheses. The response must be a one-line text file without line breaks, containing only a parameter named text: followed by the translated text. At the end of this text, add '--' exactly as indicated. Then, include a variable named original_language: followed by the original language of the translated text '{%s}' {{(%s)}}, Make sure that all of the above is true and that no new characters or anything like that have been added.Limit the translation to what I have written, do not invent words.\"}]}", request.getText(), request.getTargetLanguage());
        return new HttpEntity<>(body, headers);
    }

    public String translateText(TextTranslationRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = httpBody(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/chat/completions", HttpMethod.POST, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode messageNode = root.path("choices").get(0).path("message");
            return messageNode.path("content").asText();
        } catch (Exception e) {
            return "Translation failed";
        }
    }
}