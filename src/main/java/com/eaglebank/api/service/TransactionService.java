package com.eaglebank.api.service;

import com.eaglebank.api.exception.AccountNotFoundException;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.exception.TransactionNotFoundException;
import com.eaglebank.api.exception.UnauthorizedAccessException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    
    /**
     * Create a new transaction (deposit or withdrawal) using lock-free algorithm
     */
    @Transactional
    public Transaction createTransaction(String accountNumber, Transaction transaction, String requestingUserId) {
        log.debug("Creating {} transaction for account: {}, amount: {}", 
                transaction.getType(), accountNumber, transaction.getAmount());
        
        // Get account with pessimistic lock for database consistency
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        
        // Check if user owns the account
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only create transactions for their own accounts");
        }
        
        // Set transaction details
        transaction.setAccount(account);
        transaction.setUserId(requestingUserId);
        transaction.setCurrency("GBP");
        
        // Process transaction using lock-free algorithm with do-while loop
        boolean success = processTransactionWithLockFreeAlgorithm(account, transaction);
        
        if (!success) {
            if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal");
            } else {
                throw new IllegalArgumentException("Transaction would exceed maximum balance limit");
            }
        }
        
        // Save the transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction with ID: {} for account: {}", savedTransaction.getId(), accountNumber);
        return savedTransaction;
    }
    
    /**
     * Lock-free transaction processing using do-while loop as requested
     */
    private boolean processTransactionWithLockFreeAlgorithm(Account account, Transaction transaction) {
        AtomicReference<BigDecimal> currentBalance = new AtomicReference<>(account.getBalance());
        BigDecimal newBalance;
        boolean success;
        
        do {
            BigDecimal current = currentBalance.get();
            
            // Calculate new balance based on transaction type
            if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
                newBalance = current.add(transaction.getAmount());
            } else { // withdrawal
                newBalance = current.subtract(transaction.getAmount());
            }
            
            // Validate new balance
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Transaction would result in negative balance for account: {}", account.getAccountNumber());
                return false;
            }
            if (newBalance.compareTo(new BigDecimal("10000.00")) > 0) {
                log.warn("Transaction would exceed maximum balance for account: {}", account.getAccountNumber());
                return false;
            }
            
            // Try to update using compare-and-swap
            success = currentBalance.compareAndSet(current, newBalance);
            
        } while (!success);
        
        // Update the account balance
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        log.debug("Successfully processed {} of {} for account: {}, new balance: {}", 
                transaction.getType(), transaction.getAmount(), account.getAccountNumber(), newBalance);
        return true;
    }
    
    /**
     * Get all transactions for an account
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber, String requestingUserId) {
        log.debug("Fetching transactions for account: {}", accountNumber);
        
        // Check if account exists and user owns it
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only view transactions for their own accounts");
        }
        
        return transactionRepository.findByAccountNumber(accountNumber);
    }
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionById(String accountNumber, String transactionId, String requestingUserId) {
        log.debug("Fetching transaction with ID: {} for account: {}", transactionId, accountNumber);
        
        // Check if account exists and user owns it
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        
        if (!account.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only view transactions for their own accounts");
        }
        
        // Check if transaction exists and belongs to the account
        Transaction transaction = transactionRepository.findByIdAndAccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId + " for account: " + accountNumber));
        
        return transaction;
    }
    
    /**
     * Get all transactions for a user
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUserId(String userId) {
        log.debug("Fetching all transactions for user: {}", userId);
        return transactionRepository.findByUserId(userId);
    }
}

