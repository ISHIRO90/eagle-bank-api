package com.eaglebank.api.controller;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Authentication request received for email: {}", authRequest.getEmail());
        AuthResponse response = authService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }
}

