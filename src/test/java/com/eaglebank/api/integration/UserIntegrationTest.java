package com.eaglebank.api.integration;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.model.Address;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    private String authToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        userRepository.deleteAll();
        
        // Create test user
        Address address = new Address("123 Main St", null, null, "London", "Greater London", "SW1A 1AA");
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("+447700900123");
        testUser.setAddress(address);
        testUser.setPasswordHash("password123");
        
        // Create user via API
        String userJson = objectMapper.writeValueAsString(testUser);
        MvcResult createResult = mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseJson = createResult.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseJson, User.class);
        testUser.setId(createdUser.getId());
        
        // Authenticate to get token
        AuthRequest authRequest = new AuthRequest("john.doe@example.com", "password123");
        String authJson = objectMapper.writeValueAsString(authRequest);
        MvcResult authResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson))
                .andExpect(status().isOk())
                .andReturn();
        
        String authResponseJson = authResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(authResponseJson, AuthResponse.class);
        authToken = authResponse.getToken();
    }
    
    @Test
    void createUser_Success() throws Exception {
        Address address = new Address("456 Oak St", null, null, "Manchester", "Greater Manchester", "M1 1AA");
        User newUser = new User();
        newUser.setName("Jane Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setPhoneNumber("+447700900456");
        newUser.setAddress(address);
        newUser.setPasswordHash("password456");
        
        String userJson = objectMapper.writeValueAsString(newUser);
        
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900456"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdTimestamp").exists())
                .andExpect(jsonPath("$.updatedTimestamp").exists());
    }
    
    @Test
    void createUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        Address address = new Address("456 Oak St", null, null, "Manchester", "Greater Manchester", "M1 1AA");
        User duplicateUser = new User();
        duplicateUser.setName("Jane Smith");
        duplicateUser.setEmail("john.doe@example.com"); // Same email as existing user
        duplicateUser.setPhoneNumber("+447700900456");
        duplicateUser.setAddress(address);
        duplicateUser.setPasswordHash("password456");
        
        String userJson = objectMapper.writeValueAsString(duplicateUser);
        
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User with email john.doe@example.com already exists"));
    }
    
    @Test
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/v1/users/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900123"));
    }
    
    @Test
    void getUserById_Unauthorized_ReturnsForbidden() throws Exception {
        // Create another user
        Address address = new Address("456 Oak St", null, null, "Manchester", "Greater Manchester", "M1 1AA");
        User anotherUser = new User();
        anotherUser.setName("Jane Smith");
        anotherUser.setEmail("jane.smith@example.com");
        anotherUser.setPhoneNumber("+447700900456");
        anotherUser.setAddress(address);
        anotherUser.setPasswordHash("password456");
        
        String userJson = objectMapper.writeValueAsString(anotherUser);
        MvcResult createResult = mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseJson = createResult.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseJson, User.class);
        
        // Try to access another user's data
        mockMvc.perform(get("/v1/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User can only access their own data"));
    }
    
    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/users/usr-nonexistent")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: usr-nonexistent"));
    }
    
    @Test
    void updateUser_Success() throws Exception {
        User updatedUser = new User();
        updatedUser.setName("John Updated");
        updatedUser.setPhoneNumber("+447700900999");
        
        String userJson = objectMapper.writeValueAsString(updatedUser);
        
        mockMvc.perform(patch("/v1/users/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900999"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com")); // Should remain unchanged
    }
    
    @Test
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/v1/users/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify user is deleted
        mockMvc.perform(get("/v1/users/" + testUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void deleteUser_WithoutAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/v1/users/" + testUser.getId()))
                .andExpect(status().isUnauthorized());
    }
}

