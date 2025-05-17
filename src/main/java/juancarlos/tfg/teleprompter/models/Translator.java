package juancarlos.tfg.teleprompter.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Data
public class Translator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String originalLanguage;
    private String targetLanguage;
    private LocalDate createdDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
