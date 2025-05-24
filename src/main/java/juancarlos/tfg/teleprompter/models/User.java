package juancarlos.tfg.teleprompter.models;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entity class representing a User in the system.
 * This class stores user information including authentication details and account status.
 *
 * @author Juan Carlos
 */
@Entity
@Data
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username of the user.
     */
    private String username;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * Hashed password of the user.
     */
    private String password;

    /**
     * Role of the user in the system.
     */
    private String role;

    /**
     * Date when the user account was created.
     */
    private LocalDate createdDate;

    /**
     * Date when the user account was last updated.
     */
    private LocalDate updatedDate;

    /**
     * Date of the user's last login.
     */
    private LocalDate lastLoginDate;

    /**
     * Flag indicating if the user's email is verified.
     */
    private boolean verified;

    /**
     * Authentication token for the user.
     */
    private String token;

    /**
     * Expiry date of the authentication token.
     */
    private LocalDate tokenExpiryDate;

    /**
     * Returns a string representation of the User object.
     *
     * @author Juan Carlos
     * @return String representation of the User object
     */
    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + ", email='" + email + '\'' +
                ", password='" + password + '\'' + ", role='" + role + '\'' + ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate + ", lastLoginDate=" + lastLoginDate + ", verified=" + verified +
                ", token='" + token + '\'' + ", tokenExpiryDate=" + tokenExpiryDate + '}';
    }
}