package com.eaglebank.api.repository;

import com.eaglebank.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if user has any accounts
     */
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.user.id = :userId")
    boolean hasAccounts(@Param("userId") String userId);
}

