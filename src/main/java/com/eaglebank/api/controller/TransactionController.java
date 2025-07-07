package com.eaglebank.api.controller;

import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.service.TransactionService;
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
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction", description = "Transaction management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    @Operation(summary = "Create a new transaction (deposit or withdrawal)")
    public ResponseEntity<Transaction> createTransaction(@PathVariable String accountNumber,
                                                       @Valid @RequestBody Transaction transaction,
                                                       Authentication authentication) {
        log.info("Creating {} transaction for account: {}, amount: {}", 
                transaction.getType(), accountNumber, transaction.getAmount());
        String requestingUserId = authentication.getName();
        Transaction createdTransaction = transactionService.createTransaction(accountNumber, transaction, requestingUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }
    
    @GetMapping
    @Operation(summary = "List all transactions for an account")
    public ResponseEntity<List<Transaction>> listTransactions(@PathVariable String accountNumber,
                                                            Authentication authentication) {
        log.info("Listing transactions for account: {}", accountNumber);
        String requestingUserId = authentication.getName();
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber, requestingUserId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String accountNumber,
                                                        @PathVariable String transactionId,
                                                        Authentication authentication) {
        log.info("Fetching transaction with ID: {} for account: {}", transactionId, accountNumber);
        String requestingUserId = authentication.getName();
        Transaction transaction = transactionService.getTransactionById(accountNumber, transactionId, requestingUserId);
        return ResponseEntity.ok(transaction);
    }
}

