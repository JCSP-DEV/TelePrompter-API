package juancarlos.tfg.teleprompter.models;

import lombok.Data;

/**
 * Data transfer object for text translation requests.
 * This class represents a request to translate text to a target language.
 *
 * @author Juan Carlos
 */
@Data
public class TextTranslationRequest {
    /**
     * The text to be translated.
     */
    private String text;

    /**
     * The target language code for translation.
     */
    private String targetLanguage;
}