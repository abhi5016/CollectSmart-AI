package com.collectsmart.service;

import com.collectsmart.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

@Value("${app.jwt.secret}")
private String jwtSecret;

@Value("${app.jwt.expiration-minutes:60}")
private long expirationMinutes;

private SecretKey getSigningKey() {
    if (jwtSecret == null || jwtSecret.isBlank()
            || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
        throw new IllegalStateException(
                "JWT secret must contain at least 32 characters. Configure APP_JWT_SECRET."
        );
    }

    return Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
    );
}

public String generateToken(AppUser user) {

    Date issuedAt = new Date();
    Date expiration = new Date(
            issuedAt.getTime() + expirationMinutes * 60 * 1000
    );

    return Jwts.builder()
            .subject(user.getEmail())
            .claim("role", user.getRole())
            .claim("userId", user.getId())
            .claim("fieldOfficerId", user.getFieldOfficerId())
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(getSigningKey())
            .compact();
}

public String extractEmail(String token) {
    return extractAllClaims(token).getSubject();
}

public String extractRole(String token) {
    return extractAllClaims(token).get("role", String.class);
}

public Long extractFieldOfficerId(String token) {
    return extractAllClaims(token).get("fieldOfficerId", Long.class);
}

public boolean isTokenValid(String token, String email) {
    try {
        String tokenEmail = extractEmail(token);

        return tokenEmail.equals(email)
                && !isTokenExpired(token);

    } catch (Exception exception) {
        return false;
    }
}

private boolean isTokenExpired(String token) {
    return extractAllClaims(token)
            .getExpiration()
            .before(new Date());
}

private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}

}