package com.cagri.hrms.security;

import com.cagri.hrms.entity.core.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converts a user's role into a list of granted authorities
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
    }

    @Override
    public String getPassword() {
        // Returns the user's hashed password
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Uses email as the unique username
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // implement expiration logic here if needed
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Returns false if the account is locked (customize as needed)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Indicates whether the user's credentials are still valid
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Controls whether the user is active (e.g., email verified)
        return user.isEnabled();
    }

    public Long getId() {
        // Useful for extracting user ID in token creation or elsewhere
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public Long getCompanyId() {
        return user.getCompany() != null ? user.getCompany().getId() : null;
    }
}
