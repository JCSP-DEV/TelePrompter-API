package juancarlos.tfg.teleprompter.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class SupportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String subject;
    private String content;
    private String supportType;
    private LocalDate createDate;
    private String severity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
