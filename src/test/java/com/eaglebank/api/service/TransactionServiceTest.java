package com.eaglebank.api.service;

import com.eaglebank.api.exception.AccountNotFoundException;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.exception.TransactionNotFoundException;
import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.AccountType;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private User testUser;
    private Account testAccount;
    private Transaction testTransaction;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("usr-12345678");
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        
        testAccount = new Account();
        testAccount.setAccountNumber("01234567");
        testAccount.setSortCode("10-10-10");
        testAccount.setName("Personal Account");
        testAccount.setAccountType(AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrency("GBP");
        testAccount.setUser(testUser);
        testAccount.setCreatedTimestamp(LocalDateTime.now());
        testAccount.setUpdatedTimestamp(LocalDateTime.now());
        
        testTransaction = new Transaction();
        testTransaction.setId("tan-12345678");
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setCurrency("GBP");
        testTransaction.setType(Transaction.TransactionType.DEPOSIT);
        testTransaction.setReference("Test deposit");
        testTransaction.setUserId("usr-12345678");
        testTransaction.setAccount(testAccount);
        testTransaction.setCreatedTimestamp(LocalDateTime.now());
    }
    
    @Test
    void createTransaction_Deposit_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // When
        Transaction result = transactionService.createTransaction(accountNumber, testTransaction, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(testTransaction);
    }
    
    @Test
    void createTransaction_Withdrawal_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        testTransaction.setType(Transaction.TransactionType.WITHDRAWAL);
        testTransaction.setAmount(new BigDecimal("500.00"));
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // When
        Transaction result = transactionService.createTransaction(accountNumber, testTransaction, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(Transaction.TransactionType.WITHDRAWAL, result.getType());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(testTransaction);
    }
    
    @Test
    void createTransaction_InsufficientFunds_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        testTransaction.setType(Transaction.TransactionType.WITHDRAWAL);
        testTransaction.setAmount(new BigDecimal("2000.00")); // More than account balance
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.createTransaction(accountNumber, testTransaction, requestingUserId));
        assertEquals("Insufficient funds for withdrawal", exception.getMessage());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void createTransaction_AccountNotFound_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.empty());
        
        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> transactionService.createTransaction(accountNumber, testTransaction, requestingUserId));
        assertEquals("Account not found with number: " + accountNumber, exception.getMessage());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void createTransaction_UnauthorizedAccess_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-87654321"; // Different user
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> transactionService.createTransaction(accountNumber, testTransaction, requestingUserId));
        assertEquals("User can only create transactions for their own accounts", exception.getMessage());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void createTransaction_ExceedsMaximumBalance_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        testAccount.setBalance(new BigDecimal("9500.00")); // Close to maximum
        testTransaction.setAmount(new BigDecimal("1000.00")); // Would exceed 10000 limit
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(accountNumber, testTransaction, requestingUserId));
        assertEquals("Transaction would exceed maximum balance limit", exception.getMessage());
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void getTransactionsByAccountNumber_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountNumber(accountNumber)).thenReturn(expectedTransactions);
        
        // When
        List<Transaction> result = transactionService.getTransactionsByAccountNumber(accountNumber, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        verify(accountRepository).findById(accountNumber);
        verify(transactionRepository).findByAccountNumber(accountNumber);
    }
    
    @Test
    void getTransactionsByAccountNumber_UnauthorizedAccess_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-87654321";
        
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> transactionService.getTransactionsByAccountNumber(accountNumber, requestingUserId));
        assertEquals("User can only view transactions for their own accounts", exception.getMessage());
        verify(accountRepository).findById(accountNumber);
        verify(transactionRepository, never()).findByAccountNumber(anyString());
    }
    
    @Test
    void getTransactionById_Success() {
        // Given
        String accountNumber = "01234567";
        String transactionId = "tan-12345678";
        String requestingUserId = "usr-12345678";
        
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber))
                .thenReturn(Optional.of(testTransaction));
        
        // When
        Transaction result = transactionService.getTransactionById(accountNumber, transactionId, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        verify(accountRepository).findById(accountNumber);
        verify(transactionRepository).findByIdAndAccountNumber(transactionId, accountNumber);
    }
    
    @Test
    void getTransactionById_TransactionNotFound_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String transactionId = "tan-12345678";
        String requestingUserId = "usr-12345678";
        
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber))
                .thenReturn(Optional.empty());
        
        // When & Then
        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(accountNumber, transactionId, requestingUserId));
        assertEquals("Transaction not found with ID: " + transactionId + " for account: " + accountNumber, 
                exception.getMessage());
        verify(accountRepository).findById(accountNumber);
        verify(transactionRepository).findByIdAndAccountNumber(transactionId, accountNumber);
    }
    
    @Test
    void getTransactionsByUserId_Success() {
        // Given
        String userId = "usr-12345678";
        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);
        
        when(transactionRepository.findByUserId(userId)).thenReturn(expectedTransactions);
        
        // When
        List<Transaction> result = transactionService.getTransactionsByUserId(userId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        verify(transactionRepository).findByUserId(userId);
    }
}

