package juancarlos.tfg.teleprompter.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;

    private String password;
    private String role;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private LocalDate lastLoginDate;
    private boolean verified;
    private String token;
    private LocalDate tokenExpiryDate;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", lastLoginDate=" + lastLoginDate +
                ", verified=" + verified +
                ", token='" + token + '\'' +
                ", tokenExpiryDate=" + tokenExpiryDate +
                '}';
    }
}