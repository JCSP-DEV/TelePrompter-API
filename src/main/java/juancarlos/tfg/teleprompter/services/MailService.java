package juancarlos.tfg.teleprompter.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MailService {

    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verificación de correo electrónico";
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>¡Gracias por registrarte!</h2>" +
                "<p>Tu código de verificación es:</p>" +
                "<h1 style='color: #4CAF50;'>" + token + "</h1>" +
                "<p>Ingresa este código en la plataforma para activar tu cuenta.</p>" +
                "<br><p>Si no solicitaste esto, ignora este mensaje.</p>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("emailverificator@jc-sp.me");

            mailSender.send(message);
            System.out.println("Correo de verificación enviado a " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar correo a " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "Recuperación de contraseña";
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Solicitud de recuperación de contraseña</h2>" +
                "<p>Tu código de recuperación es:</p>" +
                "<h1 style='color: #4CAF50;'>" + token + "</h1>" +
                "<p>Ingresa este código en la plataforma para restablecer tu contraseña.</p>" +
                "<br><p>Si no solicitaste esto, ignora este mensaje.</p>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("passwordreset@jc-sp.me");

            mailSender.send(message);
            System.out.println("Correo de recuperación enviado a " + email);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar correo a " + email + ": " + e.getMessage());
        }
    }
}
