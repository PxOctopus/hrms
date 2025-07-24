package com.cagri.hrms.service.impl;

import com.cagri.hrms.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    // Sender address is injected from .env
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendCompanyVerificationEmail(String companyEmail, String managerFullName, String companyName, String token) {
        String subject = "New Registration Confirmation - " + companyName;
        String verificationLink = "http://localhost:3000/verify?token=" + token;

        String content = """
            <p>Dear Representative,</p>
            <p><strong>%s</strong> has registered on our platform on behalf of <strong>%s</strong>.</p>
            <p>Please click the link below to activate the account:</p>
            <p><a href="%s">Activate Account</a></p>
            <p>Thank you.</p>
            """.formatted(managerFullName, companyName, verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail); // Set sender
            helper.setTo(companyEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send company verification email", e);
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail); // Set sender
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
            throw new IllegalStateException("Failed to send verification email", e);
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
            helper.setFrom(fromEmail); // Set sender
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }

    @Async
    @Override
    public void sendRejectionEmail(String to, String companyName) {
        String subject = "Company Registration Rejected";
        String body = """
        <p>Dear User,</p>
        <p>We regret to inform you that your registration request for the company <strong>%s</strong> has been rejected by the administrator.</p>
        <p>If you believe this was a mistake or have any questions, please contact support.</p>
        <p>Thank you for your understanding.</p>
        """.formatted(companyName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send rejection email", e);
        }
    }

    @Async
    @Override
    public void sendApprovalEmail(String to, String companyName) {
        String subject = "Company Registration Approved";
        String body = """
        <p>Dear User,</p>
        <p>Your registration request for the company <strong>%s</strong> has been approved.</p>
        <p>You can now log in and start using the platform.</p>
        <p><a href="http://localhost:3000/login">Go to Login</a></p>
        <p>Thank you for joining us.</p>
        """.formatted(companyName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send approval email", e);
        }
    }
}