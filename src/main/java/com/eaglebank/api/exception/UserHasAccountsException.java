package com.eaglebank.api.exception;

public class UserHasAccountsException extends RuntimeException {
    public UserHasAccountsException(String message) {
        super(message);
    }
    
    public UserHasAccountsException(String message, Throwable cause) {
        super(message, cause);
    }
}

