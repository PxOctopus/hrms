package com.cagri.hrms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;

public final class SecurityUtil {
    private SecurityUtil() {}

    /** Returns current Authentication or empty if none. */
    public static Optional<Authentication> auth() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /** Returns true if the current user has role (expects simple name, e.g. "MANAGER"). */
    public static boolean hasRole(String roleSimpleName) {
        return auth()
                .map(Authentication::getAuthorities)
                .map((Collection<? extends GrantedAuthority> a) ->
                        a.stream().anyMatch(ga -> {
                            String r = ga.getAuthority(); // usually like "ROLE_MANAGER"
                            return r.equals("ROLE_" + roleSimpleName) || r.equals(roleSimpleName);
                        })
                ).orElse(false);
    }

    /** Returns the principal object (CustomUserDetails or String email) if present. */
    public static Optional<Object> principal() {
        return auth().map(Authentication::getPrincipal);
    }
}
