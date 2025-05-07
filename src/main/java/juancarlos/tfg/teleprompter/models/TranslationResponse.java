package juancarlos.tfg.teleprompter.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private String text;
    private String originalLanguage;
    private String error;
    private String message;

    public static TranslationResponse success(String text, String originalLanguage) {
        return new TranslationResponse(text, originalLanguage, null, null);
    }

    public static TranslationResponse error(String error, String message) {
        return new TranslationResponse(null, null, error, message);
    }
} 