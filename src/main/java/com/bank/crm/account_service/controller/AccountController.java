package com.bank.crm.account_service.controller;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.dto.UpdateAccountRequest;
import com.bank.crm.account_service.exception.AccountNotFoundException;
import com.bank.crm.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    /**
     * Create Account - POST /api/accounts
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            logger.info("Received request to create account for client: {}", request.getClientId());

            // Validate request parameters
            if (request.getClientId() == null) {
                throw new IllegalArgumentException("Client ID cannot be null");
            }

            if (request.getInitialDeposit() != null && request.getInitialDeposit().doubleValue() < 0) {
                throw new IllegalArgumentException("Initial deposit cannot be negative");
            }

            AccountResponse response = accountService.createAccount(request);
            logger.info("Account created successfully with ID: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameters: {}", e.getMessage());
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            logger.error("Unexpected error creating account for client {}: {}", request.getClientId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create account", e);
        }
    }

    /**
     * Delete Account - DELETE /api/accounts/{accountId}
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
        try {
            logger.info("Received request to delete account: {}", accountId);

            if (accountId == null) {
                throw new IllegalArgumentException("Account ID cannot be null");
            }

            accountService.deleteAccount(accountId);
            logger.info("Account deleted successfully: {}", accountId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (AccountNotFoundException e) {
            logger.warn("Attempted to delete non-existent account: {}", accountId);
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            logger.error("Unexpected error deleting account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    /**
     * Get Account by Client ID - GET /api/accounts/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClientId(@PathVariable UUID clientId) {
        try {
            logger.info("Received request to get accounts for client: {}", clientId);

            if (clientId == null) {
                throw new IllegalArgumentException("Client ID cannot be null");
            }

            List<AccountResponse> accounts = accountService.getAccountsByClientId(clientId);
            logger.info("Retrieved {} accounts for client: {}", accounts.size(), clientId);
            return new ResponseEntity<>(accounts, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid client ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving accounts for client {}: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve accounts for client", e);
        }
    }

    /**
     * Get All Accounts - GET /api/accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        try {
            logger.info("Received request to get all accounts");

            List<AccountResponse> accounts = accountService.getAllAccounts();
            logger.info("Retrieved {} total accounts", accounts.size());
            return new ResponseEntity<>(accounts, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Unexpected error retrieving all accounts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve accounts", e);
        }
    }

    /**
     * Get Account by ID - GET /api/accounts/{accountId}
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID accountId) {
        try {
            logger.info("Received request to get account: {}", accountId);

            if (accountId == null) {
                throw new IllegalArgumentException("Account ID cannot be null");
            }

            Optional<AccountResponse> account = accountService.getAccountById(accountId);
            if (account.isPresent()) {
                logger.info("Account found: {}", accountId);
                return new ResponseEntity<>(account.get(), HttpStatus.OK);
            } else {
                logger.warn("Account not found: {}", accountId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid account ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve account", e);
        }
    }

    /**
     * Update Account - PUT /api/accounts/{accountId}
     */
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable UUID accountId,
                                                        @Valid @RequestBody UpdateAccountRequest request) {
        try {
            logger.info("Received request to update account: {}", accountId);

            if (accountId == null) {
                throw new IllegalArgumentException("Account ID cannot be null");
            }

            // Validate request parameters
            if (request.getInitialDeposit() != null && request.getInitialDeposit().doubleValue() < 0) {
                throw new IllegalArgumentException("Initial deposit cannot be negative");
            }

            AccountResponse response = accountService.updateAccount(accountId, request);
            logger.info("Account updated successfully: {}", accountId);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccountNotFoundException e) {
            logger.warn("Attempted to update non-existent account: {}", accountId);
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameters: {}", e.getMessage());
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            logger.error("Unexpected error updating account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to update account", e);
        }
    }
}
