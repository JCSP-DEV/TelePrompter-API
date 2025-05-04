package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.TelePrompter;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.PrompterResposirtoy;
import juancarlos.tfg.teleprompter.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TelePrompterService {

    private final PrompterResposirtoy prompterResposirtoy;
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads";

    public boolean create(TelePrompter telePrompter, String userName) {
        log.info("Creating teleprompter for user: {}", userName);
        Optional<User> user = userRepository.findByUsername(userName);

        if (user.isEmpty() || prompterResposirtoy.findByNameAndUserId(telePrompter.getName(), user.get().getId()).isPresent()) {
            log.warn("User not found or teleprompter already exists");
            return false;
        }

        try {
            // Process file if provided
            MultipartFile file = telePrompter.getFile();
            if (file != null && !file.isEmpty()) {
                // Create user-specific upload directory
                Path userUploadPath = Paths.get(UPLOAD_DIR, userName);
                if (!Files.exists(userUploadPath)) {
                    Files.createDirectories(userUploadPath);
                    log.info("Created user upload directory: {}", userUploadPath);
                }

                // Save the file
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = userUploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                log.info("File saved to: {}", filePath);

                // Store file information
                telePrompter.setFilePath(filePath.toString());
                telePrompter.setFileName(file.getOriginalFilename());

                // Extract content based on file type
                String content = extractContentFromFile(filePath.toFile(), file.getContentType());
                if (content != null) {
                    log.info("Content extracted successfully, length: {}", content.length());
                    telePrompter.setContent(content);
                } else {
                    log.warn("Failed to extract content from file");
                }

            }

            telePrompter.setCreatedDate(LocalDate.now());
            telePrompter.setUser(user.get());

            log.info("Saving teleprompter: {}", telePrompter);
            TelePrompter saved = prompterResposirtoy.save(telePrompter);
            log.info("Teleprompter saved with ID: {}", saved.getId());
            return true;

        } catch (IOException e) {
            log.error("Error creating teleprompter", e);
            return false;
        }
    }

    private String extractContentFromFile(File file, String contentType) throws IOException {
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
            } else if (contentType.equals("application/pdf")) {
                try (PDDocument document = PDDocument.load(Files.newInputStream(file.toPath()))) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    content = stripper.getText(document);
                }
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()))) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    content = extractor.getText();
                }
            } else {
                log.warn("Unsupported content type: {}", contentType);
                return null;
            }

            // Remove line breaks and extra spaces
            content = content.replaceAll("\\r\\n|\\r|\\n", " ")
                           .replaceAll("\\s+", " ")
                           .trim();

            log.info("Extracted content, length: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("Error extracting content from file", e);
            return null;
        }
    }

    public List<TelePrompter> getPrompters(String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            return List.of();
        }
        
        List<TelePrompter> prompters = prompterResposirtoy.findByUser(user.get());
        return prompters.stream()
                .map(prompter -> {
                    TelePrompter simplified = new TelePrompter();
                    simplified.setId(prompter.getId());
                    simplified.setName(prompter.getName());
                    simplified.setDescription(prompter.getDescription());
                    return simplified;
                })
                .toList();
    }

    public TelePrompter getPrompterById(Long id, String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            return null;
        }

        Optional<TelePrompter> telePrompter = prompterResposirtoy.findByIdAndUser(id, user.get());
        return telePrompter.orElse(null);
    }

    public ResponseEntity<?> downloadFile(TelePrompter telePrompter) {
        File file = new File(telePrompter.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.status(404).body(Map.of("message", "❌ File not found"));
        }

        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            byte[] data = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + telePrompter.getFileName() + "\"")
                    .body(data);
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(500).body(Map.of("message", "❌ Internal server error"));
        }
    }
}
