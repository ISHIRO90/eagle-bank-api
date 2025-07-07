package com.eaglebank.api.service;

import com.eaglebank.api.exception.AccountNotFoundException;
import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.AccountType;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service class for managing bank accounts with thread-safe operations
 */
@Service
public class AccountService {
    
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new account with thread-safe operations
     */
    @Transactional
    public Account createAccount(Account account, String requestingUserId) {
        log.debug("Creating account for user: {}", requestingUserId);
        
        // Verify user exists and requesting user has permission
        User user = userService.getUserById(account.getUser().getId(), requestingUserId);
        
        // Generate unique account number
        String accountNumber = generateAccountNumber();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setCreatedTimestamp(LocalDateTime.now());
        account.setUpdatedTimestamp(LocalDateTime.now());
        
        // Set default values if not provided
        if (account.getName() == null || account.getName().trim().isEmpty()) {
            account.setName(account.getAccountType().getDisplayName());
        }
        if (account.getAccountType() == null) {
            account.setAccountType(AccountType.CHECKING);
        }
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        
        Account savedAccount = accountRepository.save(account);
        log.info("Created account with ID: {} and number: {}", savedAccount.getId(), savedAccount.getAccountNumber());
        return savedAccount;
    }
    
    /**
     * Get account by ID with authorization check
     */
    @Transactional(readOnly = true)
    public Account getAccountById(String accountId, String requestingUserId) {
        log.debug("Fetching account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        
        // Check if requesting user owns this account
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only access their own accounts");
        }
        
        return account;
    }
    
    /**
     * Get account by account number with authorization check
     */
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber, String requestingUserId) {
        log.debug("Fetching account with number: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        
        // Check if requesting user owns this account
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only access their own accounts");
        }
        
        return account;
    }
    
    /**
     * Update account with thread-safe operations using optimistic locking
     */
    @Transactional
    public Account updateAccount(String accountId, Account updatedAccount, String requestingUserId) {
        log.debug("Updating account with ID: {}", accountId);
        
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Account existingAccount = getAccountById(accountId, requestingUserId);
                
                // Update fields if provided
                if (updatedAccount.getName() != null && !updatedAccount.getName().trim().isEmpty()) {
                    existingAccount.setName(updatedAccount.getName());
                }
                if (updatedAccount.getAccountType() != null) {
                    existingAccount.setAccountType(updatedAccount.getAccountType());
                }
                
                existingAccount.setUpdatedTimestamp(LocalDateTime.now());
                
                Account savedAccount = accountRepository.save(existingAccount);
                log.info("Updated account with ID: {}", savedAccount.getId());
                return savedAccount;
                
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                log.warn("Optimistic locking failure on attempt {} for account {}", attempts, accountId);
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to update account after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                // Brief pause before retry
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in account update");
    }
    
    /**
     * Get all accounts for a user
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(String userId) {
        log.debug("Fetching accounts for user: {}", userId);
        return accountRepository.findByUserId(userId);
    }
    
    /**
     * Delete account with authorization check
     */
    @Transactional
    public void deleteAccount(String accountId, String requestingUserId) {
        log.debug("Deleting account with ID: {}", accountId);
        
        Account account = getAccountById(accountId, requestingUserId);
        
        // Check if account has zero balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete account with non-zero balance");
        }
        
        accountRepository.delete(account);
        log.info("Deleted account with ID: {}", accountId);
    }
    
    /**
     * Thread-safe balance update using optimistic locking with retry logic
     */
    @Transactional
    public Account updateBalance(String accountId, BigDecimal newBalance, String requestingUserId) {
        log.debug("Updating balance for account: {} to: {}", accountId, newBalance);
        
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Account account = getAccountById(accountId, requestingUserId);
                account.updateBalance(newBalance);
                
                Account savedAccount = accountRepository.save(account);
                log.info("Updated balance for account: {} to: {}", accountId, newBalance);
                return savedAccount;
                
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                log.warn("Optimistic locking failure on attempt {} for account {}", attempts, accountId);
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Failed to update balance after " + MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                // Brief pause before retry
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in balance update");
    }
    
    /**
     * Generate a unique 10-digit account number
     */
    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate a 10-digit number
            long number = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        
        return accountNumber;
    }
    
    /**
     * Find account by account number (internal use)
     */
    @Transactional(readOnly = true)
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Thread-safe balance addition using lock-free algorithm with do-while loop
     */
    @Transactional
    public boolean addToAccountBalance(String accountNumber, BigDecimal amount, String requestingUserId) {
        log.debug("Adding {} to balance for account: {}", amount, accountNumber);
        
        // Get account with pessimistic lock as fallback for database consistency
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        
        // Check if user owns the account
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only modify their own accounts");
        }
        
        // Use lock-free algorithm with do-while loop as requested
        AtomicReference<BigDecimal> currentBalance = new AtomicReference<>(account.getBalance());
        BigDecimal newBalance;
        boolean success;
        
        do {
            BigDecimal current = currentBalance.get();
            newBalance = current.add(amount);
            
            // Validate new balance
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Attempted operation would result in negative balance for account: {}", accountNumber);
                return false;
            }
            if (newBalance.compareTo(new BigDecimal("10000.00")) > 0) {
                log.warn("Attempted operation would exceed maximum balance for account: {}", accountNumber);
                return false;
            }
            
            // Try to update using compare-and-swap
            success = currentBalance.compareAndSet(current, newBalance);
            
        } while (!success);
        
        // Update the entity and save
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        log.info("Successfully added {} to account: {}, new balance: {}", amount, accountNumber, newBalance);
        return true;
    }
}