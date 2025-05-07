package juancarlos.tfg.teleprompter.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TranslationResponse {
    @JsonProperty("translated_text")
    private String translatedText;
    
    @JsonProperty("original_language")
    private String originalLanguage;
    
    @JsonProperty("target_language")
    private String targetLanguage;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("error_message")
    private String errorMessage;
} 