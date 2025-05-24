package juancarlos.tfg.teleprompter.services;

import juancarlos.tfg.teleprompter.models.Teleprompter;
import juancarlos.tfg.teleprompter.models.User;
import juancarlos.tfg.teleprompter.repositories.PrompterRepository;
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

/**
 * Service class that handles teleprompter document operations.
 * Provides functionality for creating, updating, retrieving, and deleting teleprompter documents,
 * as well as file handling and content extraction.
 *
 * @author Juan Carlos
 */
@Service
@AllArgsConstructor
@Slf4j
public class TelePrompterService {

    private static final String UPLOAD_DIR = "uploads";
    private final PrompterRepository prompterRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new teleprompter document with optional file upload.
     * Extracts content from uploaded files and stores them in the system.
     *
     * @author Juan Carlos
     * @param telePrompter The teleprompter object containing document details and optional file
     * @param userName The username of the user creating the teleprompter
     * @return true if the teleprompter was created successfully, false otherwise
     */
    public boolean create(Teleprompter telePrompter, String userName) {
        Optional<User> user = userRepository.findByUsername(userName);

        if (user.isEmpty() || prompterRepository.findByNameAndUserId(telePrompter.getName(), user.get().getId()).isPresent()) {
            log.warn("User not found or teleprompter already exists");
            return false;
        }

        try {
            MultipartFile file = telePrompter.getFile();
            if (file != null && !file.isEmpty()) {
                Path userUploadPath = Paths.get(UPLOAD_DIR, userName);
                if (!Files.exists(userUploadPath)) {
                    Files.createDirectories(userUploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = userUploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                log.info("File saved to: {}", filePath);

                telePrompter.setFilePath(filePath.toString());
                telePrompter.setFileName(file.getOriginalFilename());

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
            Teleprompter saved = prompterRepository.save(telePrompter);
            log.info("Teleprompter saved with ID: {}", saved.getId());
            return true;

        } catch (IOException e) {
            log.error("Error creating teleprompter", e);
            return false;
        }
    }

    /**
     * Extracts text content from various file types (PDF, DOCX, TXT).
     *
     * @author Juan Carlos
     * @param file The file to extract content from
     * @param contentType The MIME type of the file
     * @return The extracted text content, or null if extraction fails
     * @throws IOException if an I/O error occurs during file reading
     */
    private String extractContentFromFile(File file, String contentType) throws IOException {
        log.info("Extracting content from file: {}, type: {}", file.getName(), contentType);

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

            content = content.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s+", " ").trim();

            log.info("Extracted content, length: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("Error extracting content from file", e);
            return null;
        }
    }

    /**
     * Retrieves all teleprompter documents for a specific user.
     * Returns simplified versions of the documents without content.
     *
     * @author Juan Carlos
     * @param userName The username of the user
     * @return A list of simplified teleprompter documents
     */
    public List<Teleprompter> getPrompters(String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            return List.of();
        }

        List<Teleprompter> prompters = prompterRepository.findByUser(user.get());
        return prompters.stream().map(prompter -> {
            Teleprompter simplified = new Teleprompter();
            simplified.setId(prompter.getId());
            simplified.setName(prompter.getName());
            simplified.setDescription(prompter.getDescription());
            simplified.setFileName(prompter.getFileName());
            return simplified;
        }).toList();
    }

    /**
     * Retrieves a specific teleprompter document by ID for a user.
     *
     * @author Juan Carlos
     * @param id The ID of the teleprompter to retrieve
     * @param userName The username of the user
     * @return The teleprompter document if found, null otherwise
     */
    public Teleprompter getPrompterById(Long id, String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            return null;
        }

        Optional<Teleprompter> telePrompter = prompterRepository.findByIdAndUser(id, user.get());
        return telePrompter.orElse(null);
    }

    /**
     * Downloads the file associated with a teleprompter document.
     *
     * @author Juan Carlos
     * @param telePrompter The teleprompter document containing the file information
     * @return ResponseEntity containing the file data or an error message
     */
    public ResponseEntity<?> downloadFile(Teleprompter telePrompter) {
        File file = new File(telePrompter.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.status(404).body(Map.of("message", "❌ File not found"));
        }

        try {
            Path filePath = Paths.get(file.getAbsolutePath());
            byte[] data = Files.readAllBytes(filePath);
            return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + telePrompter.getFileName() + "\"").body(data);
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(500).body(Map.of("message", "❌ Internal server error"));
        }
    }

    /**
     * Deletes a teleprompter document and its associated file.
     *
     * @author Juan Carlos
     * @param id The ID of the teleprompter to delete
     * @param user The username of the user
     * @return true if the deletion was successful, false otherwise
     */
    public boolean delete(Long id, String user) {
        Optional<User> userOptional = userRepository.findByUsername(user);
        if (userOptional.isEmpty()) {
            return false;
        }

        Optional<Teleprompter> telePrompter = prompterRepository.findByIdAndUser(id, userOptional.get());
        if (telePrompter.isPresent()) {
            try {
                File file = new File(telePrompter.get().getFilePath());
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
            } catch (IOException e) {
                log.error("Error deleting file", e);
            }
            prompterRepository.delete(telePrompter.get());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates an existing teleprompter document.
     *
     * @author Juan Carlos
     * @param id The ID of the teleprompter to update
     * @param telePrompter The teleprompter object containing updated information
     * @param user The username of the user
     * @return true if the update was successful, false otherwise
     */
    public boolean update(Long id, Teleprompter telePrompter, String user) {
        Optional<User> userOptional = userRepository.findByUsername(user);
        if (userOptional.isEmpty()) {
            return false;
        }

        Optional<Teleprompter> existingTelePrompter = prompterRepository.findByIdAndUser(id, userOptional.get());
        if (existingTelePrompter.isPresent()) {
            Teleprompter telePrompterToUpdate = existingTelePrompter.get();

            if (telePrompter.getName() != null) {
                telePrompterToUpdate.setName(telePrompter.getName());
            }
            if (telePrompter.getDescription() != null) {
                telePrompterToUpdate.setDescription(telePrompter.getDescription());
            }
            if (telePrompter.getSpeed() != null) {
                telePrompterToUpdate.setSpeed(telePrompter.getSpeed());
            }
            if (telePrompter.getType() != null) {
                telePrompterToUpdate.setType(telePrompter.getType());
            }
            if (telePrompter.getLanguage() != null) {
                telePrompterToUpdate.setLanguage(telePrompter.getLanguage());
            }
            if (telePrompter.getContent() != null) {
                telePrompterToUpdate.setContent(telePrompter.getContent());
            }

            telePrompterToUpdate.setUpdatedDate(LocalDate.now());
            prompterRepository.save(telePrompterToUpdate);
            return true;
        } else {
            return false;
        }
    }
}
