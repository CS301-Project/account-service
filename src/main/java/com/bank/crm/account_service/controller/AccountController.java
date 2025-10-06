package com.bank.crm.account_service.controller;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
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
        logger.info("Received request to create account for client: {}", request.getClientId());

        AccountResponse response = accountService.createAccount(request);
        logger.info("Account created successfully with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Delete Account - DELETE /api/accounts/{accountId}
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
        logger.info("Received request to delete account: {}", accountId);

        accountService.deleteAccount(accountId);
        logger.info("Account deleted successfully: {}", accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get Account by Client ID - GET /api/accounts/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByClientId(@PathVariable UUID clientId) {
        logger.info("Received request to get accounts for client: {}", clientId);

        List<AccountResponse> accounts = accountService.getAccountsByClientId(clientId);
        logger.info("Retrieved {} accounts for client: {}", accounts.size(), clientId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    /**
     * Get All Accounts - GET /api/accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        logger.info("Received request to get all accounts");

        List<AccountResponse> accounts = accountService.getAllAccounts();
        logger.info("Retrieved {} total accounts", accounts.size());
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    /**
     * Get Account by ID - GET /api/accounts/{accountId}
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID accountId) {
        logger.info("Received request to get account: {}", accountId);

        Optional<AccountResponse> account = accountService.getAccountById(accountId);
        if (account.isPresent()) {
            logger.info("Account found: {}", accountId);
            return new ResponseEntity<>(account.get(), HttpStatus.OK);
        } else {
            logger.warn("Account not found: {}", accountId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
