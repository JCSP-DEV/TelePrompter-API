package juancarlos.tfg.teleprompter;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Teleprompter application.
 * This class serves as the entry point for the Spring Boot application.
 *
 * @author Juan Carlos
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TeleprompterApplication {
    /**
     * Main method that starts the Spring Boot application.
     * Loads environment variables from .env file before starting the application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        SpringApplication.run(TeleprompterApplication.class, args);
    }
}



