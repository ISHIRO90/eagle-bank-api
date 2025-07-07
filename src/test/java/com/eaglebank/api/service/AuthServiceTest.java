package com.eaglebank.api.service;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.model.User;
import com.eaglebank.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private AuthRequest authRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("usr-12345678");
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPasswordHash("hashedPassword");
        
        authRequest = new AuthRequest();
        authRequest.setEmail("john.doe@example.com");
        authRequest.setPassword("plainPassword");
    }
    
    @Test
    void authenticate_Success() {
        // Given
        String expectedToken = "jwt.token.here";
        
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getId(), testUser.getEmail())).thenReturn(expectedToken);
        
        // When
        AuthResponse result = authService.authenticate(authRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getEmail(), result.getEmail());
        
        verify(userService).findByEmail(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil).generateToken(testUser.getId(), testUser.getEmail());
    }
    
    @Test
    void authenticate_UserNotFound_ThrowsException() {
        // Given
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());
        
        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(authRequest));
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(userService).findByEmail(authRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
    
    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        // Given
        when(userService.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);
        
        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(authRequest));
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(userService).findByEmail(authRequest.getEmail());
        verify(passwordEncoder).matches(authRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
}

