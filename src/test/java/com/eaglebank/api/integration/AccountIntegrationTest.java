package com.eaglebank.api.integration;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.AccountType;
import com.eaglebank.api.model.Address;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    private User testUser;
    private String authToken;
    private Account testAccount;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        accountRepository.deleteAll();
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
        
        // Create test account
        testAccount = new Account();
        testAccount.setName("Personal Account");
        testAccount.setAccountType(AccountType.CHECKING);
        
        String accountJson = objectMapper.writeValueAsString(testAccount);
        MvcResult accountResult = mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        String accountResponseJson = accountResult.getResponse().getContentAsString();
        Account createdAccount = objectMapper.readValue(accountResponseJson, Account.class);
        testAccount.setAccountNumber(createdAccount.getAccountNumber());
    }
    
    @Test
    void createAccount_Success() throws Exception {
        Account newAccount = new Account();
        newAccount.setName("Savings Account");
        newAccount.setAccountType(AccountType.CHECKING);
        
        String accountJson = objectMapper.writeValueAsString(newAccount);
        
        mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.sortCode").value("10-10-10"))
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.createdTimestamp").exists());
    }
    
    @Test
    void createAccount_WithoutAuth_ReturnsUnauthorized() throws Exception {
        Account newAccount = new Account();
        newAccount.setName("Savings Account");
        newAccount.setAccountType(AccountType.CHECKING);
        
        String accountJson = objectMapper.writeValueAsString(newAccount);
        
        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void listAccounts_Success() throws Exception {
        mockMvc.perform(get("/v1/accounts")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").value(testAccount.getAccountNumber()))
                .andExpect(jsonPath("$[0].name").value("Personal Account"));
    }
    
    @Test
    void getAccountByNumber_Success() throws Exception {
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(testAccount.getAccountNumber()))
                .andExpect(jsonPath("$.name").value("Personal Account"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }
    
    @Test
    void getAccountByNumber_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/accounts/01999999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with number: 01999999"));
    }
    
    @Test
    void updateAccount_Success() throws Exception {
        Account updatedAccount = new Account();
        updatedAccount.setName("Updated Personal Account");
        
        String accountJson = objectMapper.writeValueAsString(updatedAccount);
        
        mockMvc.perform(patch("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Personal Account"))
                .andExpect(jsonPath("$.accountNumber").value(testAccount.getAccountNumber()));
    }
    
    @Test
    void deleteAccount_Success() throws Exception {
        mockMvc.perform(delete("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify account is deleted
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}

