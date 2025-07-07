package com.eaglebank.api.service;

import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.exception.UserHasAccountsException;
import com.eaglebank.api.exception.UserNotFoundException;
import com.eaglebank.api.model.Address;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private Address testAddress;
    
    @BeforeEach
    void setUp() {
        testAddress = new Address("123 Main St", null, null, "London", "Greater London", "SW1A 1AA");
        testUser = new User();
        testUser.setId("usr-12345678");
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("+447700900123");
        testUser.setAddress(testAddress);
        testUser.setPasswordHash("hashedPassword"); // This is the key - use "hashedPassword" not "password123"
        testUser.setCreatedTimestamp(LocalDateTime.now());
        testUser.setUpdatedTimestamp(LocalDateTime.now());
    }
    
    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("hashedPassword")).thenReturn("encodedPassword"); // Match what's in testUser
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User result = userService.createUser(testUser);
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(passwordEncoder).encode("hashedPassword"); // Verify with the actual value being passed
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> userService.createUser(testUser));
        assertEquals("User with email " + testUser.getEmail() + " already exists", exception.getMessage());
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_Success() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When
        User result = userService.getUserById(userId, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById(userId);
    }
    
    @Test
    void getUserById_UnauthorizedAccess_ThrowsException() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-87654321";
        
        // When & Then
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> userService.getUserById(userId, requestingUserId));
        assertEquals("User can only access their own data", exception.getMessage());
        verify(userRepository, never()).findById(anyString());
    }
    
    @Test
    void getUserById_UserNotFound_ThrowsException() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(userId, requestingUserId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }
    
    @Test
    void updateUser_Success() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        User updatedUser = new User();
        updatedUser.setName("Jane Doe");
        updatedUser.setEmail("jane.doe@example.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User result = userService.updateUser(userId, updatedUser, requestingUserId);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(updatedUser.getEmail());
        verify(userRepository).save(testUser);
    }
    
    @Test
    void updateUser_EmailTakenByAnotherUser_ThrowsException() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        User updatedUser = new User();
        updatedUser.setEmail("taken@example.com");
        
        User anotherUser = new User();
        anotherUser.setId("usr-87654321");
        anotherUser.setEmail("taken@example.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updatedUser.getEmail())).thenReturn(Optional.of(anotherUser));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, updatedUser, requestingUserId));
        assertEquals("Email " + updatedUser.getEmail() + " is already taken", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(updatedUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_Success() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.hasAccounts(userId)).thenReturn(false);
        
        // When
        userService.deleteUser(userId, requestingUserId);
        
        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).hasAccounts(userId);
        verify(userRepository).delete(testUser);
    }
    
    @Test
    void deleteUser_HasAccounts_ThrowsException() {
        // Given
        String userId = "usr-12345678";
        String requestingUserId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.hasAccounts(userId)).thenReturn(true);
        
        // When & Then
        UserHasAccountsException exception = assertThrows(UserHasAccountsException.class,
                () -> userService.deleteUser(userId, requestingUserId));
        assertEquals("Cannot delete user with existing bank accounts", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository).hasAccounts(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
    
    @Test
    void findByEmail_Success() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        
        // When
        Optional<User> result = userService.findByEmail(email);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository).findByEmail(email);
    }
    
    @Test
    void findByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userService.findByEmail(email);
        
        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }
}

