package com.collectsmart.controller;

import com.collectsmart.dto.LoginRequest;
import com.collectsmart.entity.AppUser;
import com.collectsmart.repository.AppUserRepository;
import com.collectsmart.service.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

private final AppUserRepository appUserRepository;
private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;

public AuthController(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
}

@PostMapping("/login")
public ResponseEntity<?> login(
        @Valid @RequestBody LoginRequest request
) {

    AppUser user = appUserRepository
            .findByEmail(request.email().trim().toLowerCase())
            .orElse(null);

    if (user == null
            || !Boolean.TRUE.equals(user.getActive())
            || !passwordEncoder.matches(
                    request.password(),
                    user.getPasswordHash()
            )) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message",
                        "Invalid email or password"
                ));
    }

    String token = jwtService.generateToken(user);

    return ResponseEntity.ok(
            Map.of(
                    "token", token,
                    "tokenType", "Bearer",
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "userId", user.getId(),
                    "fieldOfficerId",
                    user.getFieldOfficerId() == null
                            ? ""
                            : user.getFieldOfficerId(),
                    "expiresInMinutes", 60
            )
    );
}

}