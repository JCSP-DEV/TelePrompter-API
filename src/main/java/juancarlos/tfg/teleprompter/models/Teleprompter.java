package juancarlos.tfg.teleprompter.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "teleprompter")
public class Teleprompter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private LocalDate createdDate;
    private LocalDate updatedDate;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    @Transient
    private MultipartFile file;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Float speed;

    private Integer type;

    private String language;

    @Override
    public String toString() {
        return "TelePrompter{" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + ", filePath='" + filePath + '\'' + ", fileName='" + fileName + '\'' + '}';
    }
}
