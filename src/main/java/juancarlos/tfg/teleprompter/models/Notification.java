package juancarlos.tfg.teleprompter.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String content;
    private LocalDate createDate;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
