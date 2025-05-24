package juancarlos.tfg.teleprompter.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data transfer object for file translation requests.
 * This class represents a request to translate the contents of a file to a target language.
 *
 * @author Juan Carlos
 */
@Data
public class FileTranslationRequest {
    /**
     * The file to be translated.
     */
    private MultipartFile file;

    /**
     * The target language code for translation.
     */
    private String targetLanguage;
} 