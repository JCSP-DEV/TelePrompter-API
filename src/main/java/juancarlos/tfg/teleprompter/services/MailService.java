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
        String subject = "Email Verification";
        String htmlContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }" +
                ".token { background-color: #e8f5e9; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; color: #2e7d32; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>Welcome to TransPrompter!</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Thank you for registering with us. To complete your registration, please use the following verification code:</p>" +
                "<div class='token'>" + token + "</div>" +
                "<p>Enter this code in the platform to activate your account.</p>" +
                "<div class='footer'>" +
                "<p>If you didn't request this verification, please ignore this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("emailverificator@jc-sp.me");

            mailSender.send(message);
            System.out.println("Verification email sent to " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "Password Reset Request";
        String htmlContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }" +
                ".token { background-color: #e3f2fd; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; color: #1565c0; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>Password Reset Request</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p>We received a request to reset your password. Use the following code to proceed with the password reset:</p>" +
                "<div class='token'>" + token + "</div>" +
                "<p>Enter this code in the platform to reset your password.</p>" +
                "<div class='footer'>" +
                "<p>If you didn't request a password reset, please ignore this email.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("passwordreset@jc-sp.me");

            mailSender.send(message);
            System.out.println("Password reset email sent to " + email);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email to " + email + ": " + e.getMessage());
        }
    }
}
