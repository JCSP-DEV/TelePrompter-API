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

@Service
@AllArgsConstructor
public class FileTranslatorService {

    private static final String UPLOAD_DIR = "uploads";
    private final AiApiCallService aiApiCallService;

    public TranslationResponse translateFile(FileTranslationRequest request, String userName) throws IOException {
        MultipartFile file = request.getFile();
        String content = null;
        Path filePath = null;

        if (file != null && !file.isEmpty()) {
            // Create user-specific upload directory
            Path userUploadPath = Paths.get(UPLOAD_DIR, userName);
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
            }

            // Save the file
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            filePath = userUploadPath.resolve(fileName);


            Files.copy(file.getInputStream(), filePath);

            // Store file information

            // Extract content based on file type
            System.out.println(filePath);
            content = extractContentFromFile(filePath.toFile(), file.getContentType());

        }
        try {
            // Extract content from file
            if (content == null) {
                return TranslationResponse.error("Could not extract content from file", "Unsupported file format");
            }

            // Create translation request
            TextTranslationRequest translationRequest = new TextTranslationRequest();
            translationRequest.setText(content);
            translationRequest.setTargetLanguage(request.getTargetLanguage());

            // Translate the content
            return aiApiCallService.translateText(translationRequest);
        } finally {
            // Clean up temporary file
            Files.deleteIfExists(filePath);
        }
    }

    private String extractContentFromFile(File file, String contentType) {

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

            // Remove line breaks and extra spaces
            content = content.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s+", " ").trim();

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}