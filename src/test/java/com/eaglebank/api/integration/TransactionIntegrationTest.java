package com.eaglebank.api.integration;

import com.eaglebank.api.dto.AuthRequest;
import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.AccountType;
import com.eaglebank.api.model.Address;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
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
class TransactionIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    private User testUser;
    private String authToken;
    private Account testAccount;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        transactionRepository.deleteAll();
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
    void createTransaction_Deposit_Success() throws Exception {
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Initial deposit");
        
        String transactionJson = objectMapper.writeValueAsString(deposit);
        
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.reference").value("Initial deposit"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdTimestamp").exists());
        
        // Verify account balance was updated
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));
    }
    
    @Test
    void createTransaction_Withdrawal_Success() throws Exception {
        // First make a deposit
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("1000.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Initial deposit");
        
        String depositJson = objectMapper.writeValueAsString(deposit);
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositJson))
                .andExpect(status().isCreated());
        
        // Now make a withdrawal
        Transaction withdrawal = new Transaction();
        withdrawal.setAmount(new BigDecimal("300.00"));
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);
        withdrawal.setReference("ATM withdrawal");
        
        String withdrawalJson = objectMapper.writeValueAsString(withdrawal);
        
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(withdrawalJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.type").value("withdrawal"))
                .andExpect(jsonPath("$.reference").value("ATM withdrawal"));
        
        // Verify account balance was updated
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));
    }
    
    @Test
    void createTransaction_InsufficientFunds_ReturnsUnprocessableEntity() throws Exception {
        Transaction withdrawal = new Transaction();
        withdrawal.setAmount(new BigDecimal("1000.00"));
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);
        withdrawal.setReference("Large withdrawal");
        
        String transactionJson = objectMapper.writeValueAsString(withdrawal);
        
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient funds for withdrawal"));
    }
    
    @Test
    void createTransaction_ExceedsMaximumBalance_ReturnsBadRequest() throws Exception {
        Transaction largeDeposit = new Transaction();
        largeDeposit.setAmount(new BigDecimal("15000.00"));
        largeDeposit.setType(Transaction.TransactionType.DEPOSIT);
        largeDeposit.setReference("Large deposit");
        
        String transactionJson = objectMapper.writeValueAsString(largeDeposit);
        
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction would exceed maximum balance limit"));
    }
    
    @Test
    void createTransaction_AccountNotFound_ReturnsNotFound() throws Exception {
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Test deposit");
        
        String transactionJson = objectMapper.writeValueAsString(deposit);
        
        mockMvc.perform(post("/v1/accounts/01999999/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with number: 01999999"));
    }
    
    @Test
    void listTransactions_Success() throws Exception {
        // Create a few transactions
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Deposit 1");
        
        String depositJson = objectMapper.writeValueAsString(deposit);
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositJson))
                .andExpect(status().isCreated());
        
        Transaction deposit2 = new Transaction();
        deposit2.setAmount(new BigDecimal("300.00"));
        deposit2.setType(Transaction.TransactionType.DEPOSIT);
        deposit2.setReference("Deposit 2");
        
        String deposit2Json = objectMapper.writeValueAsString(deposit2);
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deposit2Json))
                .andExpect(status().isCreated());
        
        // List transactions
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("deposit"))
                .andExpect(jsonPath("$[1].type").value("deposit"));
    }
    
    @Test
    void getTransactionById_Success() throws Exception {
        // Create a transaction
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Test deposit");
        
        String transactionJson = objectMapper.writeValueAsString(deposit);
        MvcResult createResult = mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseJson = createResult.getResponse().getContentAsString();
        Transaction createdTransaction = objectMapper.readValue(responseJson, Transaction.class);
        
        // Get the transaction by ID
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions/" + createdTransaction.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTransaction.getId()))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.reference").value("Test deposit"));
    }
    
    @Test
    void getTransactionById_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions/tan-nonexistent")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with ID: tan-nonexistent for account: " + testAccount.getAccountNumber()));
    }
    
    @Test
    void createTransaction_WithoutAuth_ReturnsUnauthorized() throws Exception {
        Transaction deposit = new Transaction();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setReference("Test deposit");
        
        String transactionJson = objectMapper.writeValueAsString(deposit);
        
        mockMvc.perform(post("/v1/accounts/" + testAccount.getAccountNumber() + "/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionJson))
                .andExpect(status().isUnauthorized());
    }
}

