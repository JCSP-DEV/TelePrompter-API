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

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.base-url}")
    private String baseUrl;

    private static HttpEntity<String> httpBody(TextTranslationRequest request, HttpHeaders headers) {
        String body = String.format("{\"model\": \"deepseek/deepseek-r1:free\", \"messages\": [{\"role\": \"user\", \"content\": \"Translate the following text into the language specified within brackets and parentheses. Return the response in the following JSON format:\\n{\\\"text\\\": \\\"translated text\\\", \\\"original_language\\\": \\\"detected language\\\"}\\n\\nText to translate: '{%s}' {{(%s)}}\\n\\nImportant:\\n- Return ONLY the JSON object\\n- Do not add any additional text or characters\\n- Translate exactly what is provided, do not add or modify content\\n- Ensure the JSON is properly formatted\"}]}", request.getText(), request.getTargetLanguage());
        return new HttpEntity<>(body, headers);
    }

    public TranslationResponse translateText(TextTranslationRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = httpBody(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/chat/completions", HttpMethod.POST, entity, String.class);
            System.out.println(response.getBody());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode messageNode = root.path("choices").get(0).path("message");
            String content = messageNode.path("content").asText();
            
            // Clean the content by removing any backticks and markdown code block markers
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            
            // Parse the JSON response from the AI
            JsonNode translationNode = mapper.readTree(content);
            String translatedText = translationNode.path("text").asText();
            String originalLanguage = translationNode.path("original_language").asText();
            
            return TranslationResponse.success(translatedText, originalLanguage, request.getTargetLanguage());
        } catch (Exception e) {
            e.printStackTrace();
            return TranslationResponse.error("Translation failed", e.getMessage());
        }
    }
}