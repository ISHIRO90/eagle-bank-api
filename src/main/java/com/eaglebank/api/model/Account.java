package com.eaglebank.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Account entity representing a bank account
 */
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @Column(name = "id", length = 12)
    private String id;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Account number must be exactly 10 digits")
    @Column(name = "account_number", nullable = false, unique = true, length = 10)
    private String accountNumber;
    
    @Column(name = "sort_code", nullable = false)
    private String sortCode = "10-10-10";

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;
    
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;
    
    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Transaction> transactions = new ArrayList<>();
    
    // Constructors
    public Account() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
        this.balance = BigDecimal.ZERO;
        this.accountType = AccountType.CHECKING; // Default account type
    }
    
    public Account(String id, String accountNumber, String sortCode, String name, AccountType accountType, BigDecimal balance, User user) {
        this();
        this.id = id;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
        this.user = user;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }
    
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    
    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }
    
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    // Helper methods
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);
    }
    
    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setAccount(null);
    }
    
    /**
     * Thread-safe method to update balance using optimistic locking
     */
    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
        this.updatedTimestamp = LocalDateTime.now();
    }
    
    /**
     * Thread-safe method to add to balance
     */
    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedTimestamp = LocalDateTime.now();
    }
    
    /**
     * Thread-safe method to subtract from balance
     */
    public void subtractFromBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.updatedTimestamp = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", name='" + name + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                '}';
    }

    public void setSortCode(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSortCode'");
    }

    public void setCurrency(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCurrency'");
    }
}

