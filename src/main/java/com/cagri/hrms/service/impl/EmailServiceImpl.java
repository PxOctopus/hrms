package com.cagri.hrms.service.impl;

import com.cagri.hrms.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        String subject = "Email Verification";
        String content = "<p>Please click the link below to verify your email:</p>"
                + "<a href=\"" + verificationLink + "\">Verify Email</a>";
        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String subject = "Password Reset Request";
        String content = "<p>You have requested to reset your password.</p>"
                + "<a href=\"" + resetLink + "\">Reset Password</a>";
        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail) {
        String subject = "Welcome to Our Platform!";
        String content = "<p>Welcome! Your email has been successfully verified.</p>";
        sendHtmlEmail(toEmail, subject, content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true -> HTML format
            mailSender.send(message);
        } catch (MessagingException e) {
            // TODO: Handle this exception using a custom GlobalExceptionHandler for email sending failures
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}
