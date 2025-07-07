package com.eaglebank.api.model;

/**
 * Enumeration for different types of bank accounts
 */
public enum AccountType {
    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    BUSINESS("Business Account"),
    JOINT("Joint Account");
    
    private final String displayName;
    
    AccountType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}