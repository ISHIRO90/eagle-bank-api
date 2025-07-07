package com.eaglebank.api.service;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.model.User;
import com.eaglebank.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.debug("Authenticating user with email: {}", authRequest.getEmail());
        
        Optional<User> userOptional = userService.findByEmail(authRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        log.info("Successfully authenticated user: {}", user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}

