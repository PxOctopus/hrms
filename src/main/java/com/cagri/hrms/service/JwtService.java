package com.cagri.hrms.service;
import io.jsonwebtoken.Jwts;
import com.cagri.hrms.entity.User;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${authservice.jwt.secret-key}")
    private String secretKey;

    @Value("${authservice.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${authservice.jwt.issuer}")
    private String issuer;

    private Key key;

    // Initializes the signing key after secretKey is injected
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Generates a JWT token for the given user
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // sets email as the subject
                .claim("role", user.getRole().getName()) // include a role in claims
                .setIssuer(issuer) // sets the issuer of the token
                .setIssuedAt(new Date()) // current time as issue time
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // expiration time
                .signWith(key, SignatureAlgorithm.HS256) // sign the token with HMAC SHA-256
                .compact(); // return the token as String
    }

    // Extracts the email (subject) from a given JWT token
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key) // use the same key for validation
                    .build()
                    .parseClaimsJws(token) // parse and validate the token
                    .getBody()
                    .getSubject(); // return the subject (email)
        } catch (Exception e) { //
            // TODO: Replace this generic RuntimeException with a custom exception (e.g., InvalidTokenException)
            // This placeholder is temporary and will be refactored during global exception handling implementation.
            throw new RuntimeException("Invalid or expired JWT token");
        }
    }
}

