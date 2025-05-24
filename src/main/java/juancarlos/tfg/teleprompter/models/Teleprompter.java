package juancarlos.tfg.teleprompter.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Entity class representing a Teleprompter document.
 * This class stores information about teleprompter documents including their content,
 * metadata, and associated user information.
 *
 * @author Juan Carlos
 */
@Entity
@Data
@Table(name = "teleprompter")
public class Teleprompter {

    /**
     * Unique identifier for the teleprompter document.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the teleprompter document.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Description of the teleprompter document.
     */
    private String description;

    /**
     * Content of the teleprompter document stored as LONGTEXT.
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * Date when the document was created.
     */
    private LocalDate createdDate;

    /**
     * Date when the document was last updated.
     */
    private LocalDate updatedDate;

    /**
     * Path to the stored file.
     */
    @Column(nullable = false)
    private String filePath;

    /**
     * Name of the file.
     */
    @Column(nullable = false)
    private String fileName;

    /**
     * Temporary storage for uploaded file.
     */
    @Transient
    private MultipartFile file;

    /**
     * User who owns this teleprompter document.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Speed setting for the teleprompter.
     */
    private Integer speed;

    /**
     * Type of the teleprompter document.
     */
    private Integer type;

    /**
     * Language of the teleprompter content.
     */
    private String language;

    /**
     * Returns a string representation of the Teleprompter object.
     * Truncates the content to 50 characters for display purposes.
     *
     * @author Juan Carlos
     * @return String representation of the Teleprompter object
     */
    @Override
    public String toString() {
        return "TelePrompter{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + ", filePath='" + filePath + '\'' + ", fileName='" + fileName + '\'' + '}';
    }
}
