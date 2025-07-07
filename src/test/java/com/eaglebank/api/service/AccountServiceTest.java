package com.eaglebank.api.service;

import com.eaglebank.api.exception.AccountNotFoundException;
import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.exception.UserNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.AccountType;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
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
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    private User testUser;
    private Account testAccount;
    
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
    }
    
    @Test
    void createAccount_Success() {
        // Given
        String userId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        // When
        Account result = accountService.createAccount(testAccount, userId);
        
        // Then
        assertNotNull(result);
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
        assertEquals(testUser, result.getUser());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(userRepository).findById(userId);
        verify(accountRepository).save(testAccount);
    }
    
    @Test
    void createAccount_UserNotFound_ThrowsException() {
        // Given
        String userId = "usr-12345678";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> accountService.createAccount(testAccount, userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void getAccountsByUserId_Success() {
        // Given
        String userId = "usr-12345678";
        List<Account> expectedAccounts = Arrays.asList(testAccount);
        when(accountRepository.findByUserId(userId)).thenReturn(expectedAccounts);
        
        // When
        List<Account> result = accountService.getAccountsByUserId(userId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getAccountNumber(), result.get(0).getAccountNumber());
        verify(accountRepository).findByUserId(userId);
    }
    
    @Test
    void getAccountByNumber_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When
        Account result = accountService.getAccountByNumber(accountNumber, requestingUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository).findById(accountNumber);
    }
    
    @Test
    void getAccountByNumber_UnauthorizedAccess_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-87654321";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When & Then
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> accountService.getAccountByNumber(accountNumber, requestingUserId));
        assertEquals("User can only access their own accounts", exception.getMessage());
        verify(accountRepository).findById(accountNumber);
    }
    
    @Test
    void getAccountByNumber_AccountNotFound_ThrowsException() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());
        
        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountByNumber(accountNumber, requestingUserId));
        assertEquals("Account not found with number: " + accountNumber, exception.getMessage());
        verify(accountRepository).findById(accountNumber);
    }
    
    @Test
    void updateAccount_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        Account updatedAccount = new Account();
        updatedAccount.setName("Updated Account Name");
        updatedAccount.setAccountType(AccountType.CHECKING);
        
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        // When
        Account result = accountService.updateAccount(accountNumber, updatedAccount, requestingUserId);
        
        // Then
        assertNotNull(result);
        verify(accountRepository).findById(accountNumber);
        verify(accountRepository).save(testAccount);
    }
    
    @Test
    void deleteAccount_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When
        accountService.deleteAccount(accountNumber, requestingUserId);
        
        // Then
        verify(accountRepository).findById(accountNumber);
        verify(accountRepository).delete(testAccount);
    }
    
    @Test
    void updateAccountBalance_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        BigDecimal newBalance = new BigDecimal("2000.00");
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        // When
        boolean result = accountService.addToAccountBalance(accountNumber, newBalance, requestingUserId);
        
        // Then
        assertTrue(result);
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository).save(testAccount);
    }
    
    @Test
    void updateAccountBalance_NegativeBalance_ReturnsFalse() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        BigDecimal negativeBalance = new BigDecimal("-100.00");
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When
        boolean result = accountService.addToAccountBalance(accountNumber, negativeBalance, requestingUserId);
        
        // Then
        assertFalse(result);
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void updateAccountBalance_ExceedsMaximum_ReturnsFalse() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        BigDecimal excessiveBalance = new BigDecimal("15000.00");
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When
        boolean result = accountService.addToAccountBalance(accountNumber, excessiveBalance, requestingUserId);
        
        // Then
        assertFalse(result);
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void addToAccountBalance_Success() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        BigDecimal amount = new BigDecimal("500.00");
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        
        // When
        boolean result = accountService.addToAccountBalance(accountNumber, amount, requestingUserId);
        
        // Then
        assertTrue(result);
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository).save(testAccount);
    }
    
    @Test
    void addToAccountBalance_WouldCauseNegativeBalance_ReturnsFalse() {
        // Given
        String accountNumber = "01234567";
        String requestingUserId = "usr-12345678";
        BigDecimal largeWithdrawal = new BigDecimal("-2000.00");
        
        when(accountRepository.findByAccountNumberWithLock(accountNumber)).thenReturn(Optional.of(testAccount));
        
        // When
        boolean result = accountService.addToAccountBalance(accountNumber, largeWithdrawal, requestingUserId);
        
        // Then
        assertFalse(result);
        verify(accountRepository).findByAccountNumberWithLock(accountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }
}

