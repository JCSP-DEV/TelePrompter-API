package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TextTranslationRequest;
import juancarlos.tfg.teleprompter.models.TranslationResponse;
import juancarlos.tfg.teleprompter.models.FileTranslationRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
@Slf4j
public class FileTranslatorService {

    private final AiApiCallService aiApiCallService;
    private static final String UPLOAD_DIR = "uploads";

    public TranslationResponse translate(TextTranslationRequest request) {
        return aiApiCallService.translateText(request);
    }

    public TranslationResponse translateFile(FileTranslationRequest request, String userName) throws IOException {
        log.info("Starting file translation for user: {}", userName);
        
        // Create user-specific upload directory
        Path userUploadPath = Paths.get(UPLOAD_DIR, userName);
        if (!Files.exists(userUploadPath)) {
            Files.createDirectories(userUploadPath);
            log.info("Created user upload directory: {}", userUploadPath);
        }

        // Save the file temporarily
        String fileName = System.currentTimeMillis() + "_" + request.getFile().getOriginalFilename();
        Path filePath = userUploadPath.resolve(fileName);
        Files.copy(request.getFile().getInputStream(), filePath);
        log.info("File saved temporarily at: {}", filePath);

        try {
            // Extract content from file
            String content = extractContentFromFile(filePath.toFile(), request.getFile().getContentType());
            if (content == null) {
                log.error("Failed to extract content from file: {}", filePath);
                return TranslationResponse.error("Could not extract content from file", "Unsupported file format");
            }

            // Clean and normalize the content
            content = cleanContent(content);
            log.info("Content extracted and cleaned successfully, length: {}", content.length());
            log.debug("Extracted content: {}", content);

            // Create translation request
            TextTranslationRequest translationRequest = new TextTranslationRequest();
            translationRequest.setText(content);
            translationRequest.setTargetLanguage(request.getTargetLanguage());

            // Translate the content
            return aiApiCallService.translateText(translationRequest);
        } finally {
            // Clean up temporary file
            Files.deleteIfExists(filePath);
            log.info("Temporary file deleted: {}", filePath);
        }
    }

    private String extractContentFromFile(File file, String contentType) {
        log.info("Extracting content from file: {}, type: {}", file.getName(), contentType);
        
        try {
            // Try to detect file type from extension if content type is octet-stream
            if (contentType == null || contentType.equals("application/octet-stream")) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (fileName.endsWith(".txt")) {
                    contentType = "text/plain";
                } else if (fileName.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
                log.info("Detected content type from extension: {}", contentType);
            }

            if (contentType == null) {
                log.warn("Content type is null and could not be detected from extension");
                return null;
            }

            String content;
            if (contentType.equals("text/plain")) {
                content = new String(Files.readAllBytes(file.toPath()));
                log.info("Extracted text from plain text file");
            } else if (contentType.equals("application/pdf")) {
                try (PDDocument document = PDDocument.load(Files.newInputStream(file.toPath()))) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    content = stripper.getText(document);
                    log.info("Extracted text from PDF file");
                }
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()))) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    content = extractor.getText();
                    log.info("Extracted text from Word document");
                }
            } else {
                log.warn("Unsupported content type: {}", contentType);
                return null;
            }

            return content;
        } catch (Exception e) {
            log.error("Error extracting content from file", e);
            return null;
        }
    }

    private String cleanContent(String content) {
        if (content == null) return null;

        // Remove HTML tags if present
        content = content.replaceAll("<[^>]*>", "");
        
        // Remove special characters and normalize whitespace
        content = content.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "")
                        .replaceAll("\\s+", " ")
                        .trim();

        // Remove any remaining HTML entities
        content = content.replaceAll("&[a-zA-Z]+;", "");

        // Remove any remaining special characters that might cause JSON issues
        content = content.replaceAll("[\\p{Cntrl}]", "")
                        .replaceAll("[^\\x20-\\x7E]", "");

        return content;
    }
}