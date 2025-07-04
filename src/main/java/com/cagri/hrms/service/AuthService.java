package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.request.general.ForgotPasswordRequestDTO;
import com.cagri.hrms.dto.request.general.ResetPasswordRequestDTO;
import com.cagri.hrms.dto.request.user.VerifyEmailRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;
import jakarta.validation.Valid;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    void forgotPassword(ForgotPasswordRequestDTO request);

    void resetPassword(ResetPasswordRequestDTO request);

    void verifyEmail(@Valid VerifyEmailRequestDTO request);
}
