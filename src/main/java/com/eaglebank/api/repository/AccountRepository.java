package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    /**
     * Find all accounts for a specific user
     */
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") String userId);
    
    /**
     * Find account by account number with pessimistic lock for thread-safe operations
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    /**
     * Find account by account number without lock
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * Find account by account number and user ID
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.user.id = :userId")
    Optional<Account> findByAccountNumberAndUserId(@Param("accountNumber") String accountNumber, @Param("userId") String userId);
    
    /**
     * Check if account exists by account number
     */
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * Check if account belongs to user
     */
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.accountNumber = :accountNumber AND a.user.id = :userId")
    boolean existsByAccountNumberAndUserId(@Param("accountNumber") String accountNumber, @Param("userId") String userId);
}

