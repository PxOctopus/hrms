package com.cagri.hrms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Hardened JWT filter (email-subject flow).
 *
 * WHAT CHANGED (vs your previous version):
 *  - CHANGE: Safely handle invalid/expired tokens (try/catch around parsing) instead of throwing 500.
 *  - CHANGE: Catch UsernameNotFoundException when the user no longer exists (stale token) and continue unauthenticated.
 *  - CHANGE: Short-circuit CORS preflight (OPTIONS) requests.
 *  - CHANGE: Skip re-authentication if SecurityContext already has an Authentication.
 *  - Kept: Public path bypass via JwtPathWhiteList.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();

        // CHANGE: Skip CORS preflight requests early.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Public endpoints remain bypassed (aligned with SecurityConfig permitAll).
        if (JwtPathWhiteList.isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No bearer token -> proceed without authentication.
            filterChain.doFilter(request, response);
            return;
        }

        // CHANGE: Do not re-authenticate if already set (avoids duplicate work).
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        String userEmail;
        try {
            // CHANGE: Wrap parsing; invalid/expired token must not bubble up as 500.
            userEmail = jwtTokenProvider.extractUsername(token); // current flow: sub/email
        } catch (Exception ex) {
            // Token cannot be parsed/verified -> leave unauthenticated and continue.
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null) {
            try {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Validate token against the resolved user (signature + exp + subject match).
                if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                    final UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (UsernameNotFoundException ex) {
                // CHANGE: User deleted/renamed while a stale token exists -> treat as unauthenticated.
                SecurityContextHolder.clearContext();
            }
        }

        // Always continue the filter chain.
        filterChain.doFilter(request, response);
    }
}
