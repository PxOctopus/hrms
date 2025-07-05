package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.request.general.ForgotPasswordRequestDTO;
import com.cagri.hrms.dto.request.general.ResetPasswordRequestDTO;
import com.cagri.hrms.dto.request.user.VerifyEmailRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;
import com.cagri.hrms.dto.response.general.MessageResponseDTO;
import com.cagri.hrms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
        return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponseDTO> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new MessageResponseDTO("Email verified successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO("Password reset link sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO("Password has been reset successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout() {

        return ResponseEntity.ok(new MessageResponseDTO("Logged out successfully."));
    }
}
