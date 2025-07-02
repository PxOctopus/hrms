package com.cagri.hrms.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String verificationLink);
    void sendPasswordResetEmail(String toEmail, String resetLink);
    void sendWelcomeEmail(String toEmail);
}
