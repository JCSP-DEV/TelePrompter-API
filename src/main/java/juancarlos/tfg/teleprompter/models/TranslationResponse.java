package juancarlos.tfg.teleprompter.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for translation responses.
 * This class represents the response from a translation operation, including both successful
 * translations and error cases.
 *
 * @author Juan Carlos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    /**
     * The translated text.
     */
    private String text;

    /**
     * The original language of the text.
     */
    private String originalLanguage;

    /**
     * The target language of the translation.
     */
    private String targetLanguage;

    /**
     * Error code if translation failed.
     */
    private String error;

    /**
     * Error message if translation failed.
     */
    private String message;

    /**
     * Creates a successful translation response.
     *
     * @author Juan Carlos
     * @param text The translated text
     * @param originalLanguage The original language of the text
     * @param targetLanguage The target language of the translation
     * @return A TranslationResponse object with success information
     */
    public static TranslationResponse success(String text, String originalLanguage, String targetLanguage) {
        return new TranslationResponse(text, originalLanguage, targetLanguage, null, null);
    }

    /**
     * Creates an error translation response.
     *
     * @author Juan Carlos
     * @param error The error code
     * @param message The error message
     * @return A TranslationResponse object with error information
     */
    public static TranslationResponse error(String error, String message) {
        return new TranslationResponse(null, null, null, error, message);
    }
} 