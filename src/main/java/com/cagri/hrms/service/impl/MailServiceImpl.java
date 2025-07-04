package com.cagri.hrms.service.impl;

import com.cagri.hrms.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Verify your email");
            helper.setText(
                    "<p>Thank you for registering.</p>" +
                            "<p>Click the link below to verify your email:</p>" +
                            "<a href=\"http://localhost:3000/verify?token=" + token + "\">Verify Email</a>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        String subject = "Password Reset Request";
        String body = """
            <h2>Password Reset</h2>
            <p>You requested to reset your password. Click the link below to proceed:</p>
            <a href="%s">Reset your password</a>
            <p>If you didn’t request this, you can safely ignore this email.</p>
            """.formatted(resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }
}
