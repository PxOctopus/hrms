package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}
