package com.cagri.hrms.service;

public interface MailService {
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    void sendCompanyVerificationEmail(String companyEmail, String managerFullName, String companyName, String token);
}
