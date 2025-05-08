package juancarlos.tfg.teleprompter.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileTranslationRequest {
    private MultipartFile file;
    private String targetLanguage;
} 