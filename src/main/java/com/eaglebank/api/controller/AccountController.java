package com.eaglebank.api.controller;

import com.eaglebank.api.model.Account;
import com.eaglebank.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account", description = "Bank account management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account, Authentication authentication) {
        log.info("Creating new account for user: {}", authentication.getName());
        String userId = authentication.getName();
        Account createdAccount = accountService.createAccount(account, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }
    
    @GetMapping
    @Operation(summary = "List all accounts for the authenticated user")
    public ResponseEntity<List<Account>> listAccounts(Authentication authentication) {
        log.info("Listing accounts for user: {}", authentication.getName());
        String userId = authentication.getName();
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber, Authentication authentication) {
        log.info("Fetching account with number: {}", accountNumber);
        String requestingUserId = authentication.getName();
        Account account = accountService.getAccountByNumber(accountNumber, requestingUserId);
        return ResponseEntity.ok(account);
    }
    
    @PatchMapping("/{accountNumber}")
    @Operation(summary = "Update account by account number")
    public ResponseEntity<Account> updateAccount(@PathVariable String accountNumber, 
                                               @Valid @RequestBody Account updatedAccount, 
                                               Authentication authentication) {
        log.info("Updating account with number: {}", accountNumber);
        String requestingUserId = authentication.getName();
        Account account = accountService.updateAccount(accountNumber, updatedAccount, requestingUserId);
        return ResponseEntity.ok(account);
    }
    
    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete account by account number")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber, Authentication authentication) {
        log.info("Deleting account with number: {}", accountNumber);
        String requestingUserId = authentication.getName();
        accountService.deleteAccount(accountNumber, requestingUserId);
        return ResponseEntity.noContent().build();
    }
}

