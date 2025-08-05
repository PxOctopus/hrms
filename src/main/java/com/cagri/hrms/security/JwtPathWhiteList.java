package com.cagri.hrms.security;

import java.util.List;

public class JwtPathWhiteList {

    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    public static boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.contains(path)
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
