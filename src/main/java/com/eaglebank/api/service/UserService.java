package com.eaglebank.api.service;

import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.exception.UserHasAccountsException;
import com.eaglebank.api.exception.UserNotFoundException;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user
     */
    @Transactional
    public User createUser(User user) {
        log.debug("Creating new user with email: {}", user.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Hash the password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        User savedUser = userRepository.save(user);
        log.info("Created user with ID: {}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(String userId, String requestingUserId) {
        log.debug("Fetching user with ID: {}", userId);
        
        // Check if user is trying to access their own data
        if (!userId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only access their own data");
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
    
    /**
     * Update user
     */
    @Transactional
    public User updateUser(String userId, User updatedUser, String requestingUserId) {
        log.debug("Updating user with ID: {}", userId);
        
        // Check if user is trying to update their own data
        if (!userId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only update their own data");
        }
        
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Update fields
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getAddress() != null) {
            existingUser.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getEmail() != null) {
            // Check if new email is already taken by another user
            Optional<User> userWithEmail = userRepository.findByEmail(updatedUser.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email " + updatedUser.getEmail() + " is already taken");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        
        User savedUser = userRepository.save(existingUser);
        log.info("Updated user with ID: {}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(String userId, String requestingUserId) {
        log.debug("Deleting user with ID: {}", userId);
        
        // Check if user is trying to delete their own data
        if (!userId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only delete their own data");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        // Check if user has any accounts
        if (userRepository.hasAccounts(userId)) {
            throw new UserHasAccountsException("Cannot delete user with existing bank accounts");
        }
        
        userRepository.delete(user);
        log.info("Deleted user with ID: {}", userId);
    }
    
    /**
     * Find user by email (for authentication)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

