package com.eaglebank.api.repository;

import com.eaglebank.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    /**
     * Find all transactions for a specific account
     */
    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber ORDER BY t.createdTimestamp DESC")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * Find transaction by ID and account number
     */
    @Query("SELECT t FROM Transaction t WHERE t.id = :transactionId AND t.account.accountNumber = :accountNumber")
    Optional<Transaction> findByIdAndAccountNumber(@Param("transactionId") String transactionId, @Param("accountNumber") String accountNumber);
    
    /**
     * Find all transactions for a specific user
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.createdTimestamp DESC")
    List<Transaction> findByUserId(@Param("userId") String userId);
    
    /**
     * Check if transaction belongs to account
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.id = :transactionId AND t.account.accountNumber = :accountNumber")
    boolean existsByIdAndAccountNumber(@Param("transactionId") String transactionId, @Param("accountNumber") String accountNumber);
    
    /**
     * Check if transaction belongs to user
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.id = :transactionId AND t.userId = :userId")
    boolean existsByIdAndUserId(@Param("transactionId") String transactionId, @Param("userId") String userId);
}

