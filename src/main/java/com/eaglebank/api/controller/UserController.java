package com.eaglebank.api.controller;

import com.eaglebank.api.model.User;
import com.eaglebank.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management endpoints")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<User> getUserById(@PathVariable String userId, Authentication authentication) {
        log.info("Fetching user with ID: {}", userId);
        String requestingUserId = authentication.getName();
        User user = userService.getUserById(userId, requestingUserId);
        return ResponseEntity.ok(user);
    }
    
    @PatchMapping("/{userId}")
    @Operation(summary = "Update user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<User> updateUser(@PathVariable String userId, 
                                         @Valid @RequestBody User updatedUser, 
                                         Authentication authentication) {
        log.info("Updating user with ID: {}", userId);
        String requestingUserId = authentication.getName();
        User user = userService.updateUser(userId, updatedUser, requestingUserId);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteUser(@PathVariable String userId, Authentication authentication) {
        log.info("Deleting user with ID: {}", userId);
        String requestingUserId = authentication.getName();
        userService.deleteUser(userId, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}

