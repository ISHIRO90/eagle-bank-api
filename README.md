# Eagle Bank API

A Spring Boot REST API for banking operations with thread-safe transaction processing and comprehensive testing.

## Overview

This project implements a banking API that complies with the provided OpenAPI specification. It features:

- **Thread-safe operations**: All transactional operations use lock-free algorithms with optimistic concurrency control
- **JWT Authentication**: Secure API access with JSON Web Tokens
- **Comprehensive Testing**: Unit tests with Mockito and integration tests
- **OpenAPI Compliance**: Generated from the provided OpenAPI YAML specification
- **Spring Boot 3**: Modern Java 17 with Jakarta EE

## Architecture

### Key Components

1. **Models**: JPA entities for User, Account, and Transaction
2. **Repositories**: Spring Data JPA repositories with custom queries
3. **Services**: Business logic with thread-safe operations
4. **Controllers**: REST endpoints following OpenAPI specification
5. **Security**: JWT-based authentication and authorization

### Thread Safety

The application implements thread-safe operations using:

- **Optimistic Locking**: JPA `@Version` annotations on entities
- **Lock-free Algorithms**: Do-while loops with retry logic for concurrent updates
- **Atomic Operations**: Database-level constraints and transactions

## API Endpoints

### Authentication
- `POST /v1/auth/login` - Authenticate user and get JWT token

### Users
- `POST /v1/users` - Create new user
- `GET /v1/users/{userId}` - Get user by ID
- `PATCH /v1/users/{userId}` - Update user
- `DELETE /v1/users/{userId}` - Delete user

### Accounts
- `POST /v1/accounts` - Create new account
- `GET /v1/accounts` - List user's accounts
- `GET /v1/accounts/{accountNumber}` - Get account details
- `PATCH /v1/accounts/{accountNumber}` - Update account
- `DELETE /v1/accounts/{accountNumber}` - Delete account

### Transactions
- `POST /v1/accounts/{accountNumber}/transactions` - Create transaction
- `GET /v1/accounts/{accountNumber}/transactions` - List transactions
- `GET /v1/accounts/{accountNumber}/transactions/{transactionId}` - Get transaction

## Building and Running

### Prerequisites
- Java 17 or higher
- Gradle 8.5 or higher

### Build
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
```

### Run Application
```bash
./gradlew bootRun
```

The application will start on port 8080. Access the Swagger UI at:
http://localhost:8080/swagger-ui.html

## Configuration

### Database
- Development: H2 in-memory database
- Production: Configure `application.yml` for your database

### JWT
- Secret key: Configure in `application.yml`
- Token expiration: 24 hours (configurable)

### Security
- CORS enabled for all origins
- JWT required for all endpoints except user creation and authentication

## Testing

### Unit Tests
- **UserServiceTest**: Tests user management logic
- **AccountServiceTest**: Tests account operations and thread safety
- **TransactionServiceTest**: Tests transaction processing with lock-free algorithms
- **AuthServiceTest**: Tests authentication logic

### Integration Tests
- **UserIntegrationTest**: End-to-end user workflows
- **AccountIntegrationTest**: Account management integration
- **TransactionIntegrationTest**: Transaction processing integration

All tests use Mockito for mocking dependencies and Spring Boot Test for integration testing.

## Thread-Safe Implementation Details

### Account Balance Updates
```java
public boolean updateAccountBalance(String accountNumber, BigDecimal newBalance, String requestingUserId) {
    int maxRetries = 10;
    int retryCount = 0;
    
    do {
        Optional<Account> accountOpt = accountRepository.findByAccountNumberWithLock(accountNumber);
        if (accountOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found with number: " + accountNumber);
        }
        
        Account account = accountOpt.get();
        validateAccountAccess(account, requestingUserId);
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0 || 
            newBalance.compareTo(new BigDecimal("10000.00")) > 0) {
            return false;
        }
        
        try {
            account.setBalance(newBalance);
            account.setUpdatedTimestamp(LocalDateTime.now());
            accountRepository.save(account);
            return true;
        } catch (OptimisticLockingFailureException e) {
            retryCount++;
            if (retryCount >= maxRetries) {
                log.error("Failed to update account balance after {} retries", maxRetries);
                return false;
            }
            // Brief pause before retry
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    } while (retryCount < maxRetries);
    
    return false;
}
```

### Transaction Processing
The transaction service uses similar lock-free algorithms with retry logic to ensure thread-safe processing of deposits and withdrawals.

## Security Features

- JWT-based authentication
- User can only access their own data
- Password hashing with BCrypt
- CORS configuration for frontend integration
- Input validation and sanitization

## Error Handling

Comprehensive error handling with:
- Custom exception classes
- Global exception handler
- Proper HTTP status codes
- Detailed error messages

## Future Enhancements

- Database connection pooling optimization
- Metrics and monitoring integration
- Rate limiting for API endpoints
- Audit logging for all transactions
- Multi-currency support
- Account types and permissions

## License

This project is created as a coding test demonstration.

