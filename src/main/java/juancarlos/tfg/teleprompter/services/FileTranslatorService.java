package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.FileTranslationRequest;
import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service class that handles file translation operations.
 * Provides functionality for translating content from various file types (PDF, DOCX, TXT).
 *
 * @author Juan Carlos
 */
@Service
@AllArgsConstructor
public class FileTranslatorService {

    private static final String UPLOAD_DIR = "uploads";
    private final AiApiCallService aiApiCallService;

    /**
     * Translates the content of an uploaded file to the target language.
     * Extracts text from the file and sends it for translation.
     *
     * @param request The translation request containing the file and target language
     * @param userName The username of the user making the request
     * @return A TranslationResponse containing the translated text or error information
     * @throws IOException if an error occurs during file processing
     */
    public TranslationResponse translateFile(FileTranslationRequest request, String userName) throws IOException {
        MultipartFile file = request.getFile();
        String content = null;
        Path filePath = null;

        if (file != null && !file.isEmpty()) {
            Path userUploadPath = Paths.get(UPLOAD_DIR, userName);
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            filePath = userUploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            content = extractContentFromFile(filePath.toFile(), file.getContentType());
        }
        try {
            if (content == null) {
                return TranslationResponse.error("Could not extract content from file", "Unsupported file format");
            }

            TextTranslationRequest translationRequest = new TextTranslationRequest();
            translationRequest.setText(content);
            translationRequest.setTargetLanguage(request.getTargetLanguage());

            return aiApiCallService.translateText(translationRequest);
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    /**
     * Extracts text content from various file types.
     * Supports PDF, DOCX, and TXT files.
     *
     * @param file The file to extract content from
     * @param contentType The MIME type of the file
     * @return The extracted text content, or null if extraction fails
     */
    private String extractContentFromFile(File file, String contentType) {
        try {
            if (contentType == null || contentType.equals("application/octet-stream")) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (fileName.endsWith(".txt")) {
                    contentType = "text/plain";
                } else if (fileName.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
            }

            if (contentType == null) {
                return null;
            }

            String content;
            switch (contentType) {
                case "text/plain" -> content = new String(Files.readAllBytes(file.toPath()));
                case "application/pdf" -> {
                    try (PDDocument document = PDDocument.load(Files.newInputStream(file.toPath()))) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        content = stripper.getText(document);
                    }
                }
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                    try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()))) {
                        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                        content = extractor.getText();
                    }
                }
                default -> {
                    return null;
                }
            }

            content = content.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s+", " ").trim();

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}