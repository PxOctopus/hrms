package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.LoginRequestDTO;
import com.cagri.hrms.dto.request.RegisterRequestDTO;
import com.cagri.hrms.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}
