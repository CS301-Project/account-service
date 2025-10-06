package com.bank.crm.account_service.service;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.exception.AccountNotFoundException;
import com.bank.crm.account_service.model.Account;
import com.bank.crm.account_service.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Create a new account
     */
    public AccountResponse createAccount(CreateAccountRequest request) {
        logger.info("Creating new account for client ID: {}", request.getClientId());

        // Create new account entity
        Account account = new Account(
            request.getClientId(),
            request.getAccType(),
            request.getAccStatus(),
            LocalDateTime.now(), // Set opening date to current time
            request.getInitialDeposit(),
            request.getCurrency(),
            request.getBranchId()
        );

        // Save to database
        Account savedAccount = accountRepository.save(account);

        logger.info("Account created successfully with ID: {}", savedAccount.getId());

        return convertToResponse(savedAccount);
    }

    /**
     * Delete an account by ID
     */
    public void deleteAccount(UUID accountId) {
        logger.info("Attempting to delete account with ID: {}", accountId);

        if (!accountRepository.existsById(accountId)) {
            logger.warn("Account with ID {} not found for deletion", accountId);
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        accountRepository.deleteById(accountId);
        logger.info("Account with ID {} deleted successfully", accountId);
    }

    /**
     * Get all accounts for a specific client
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByClientId(UUID clientId) {
        logger.info("Retrieving accounts for client ID: {}", clientId);

        List<Account> accounts = accountRepository.findByClientId(clientId);

        logger.info("Found {} accounts for client ID: {}", accounts.size(), clientId);

        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all accounts
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        logger.info("Retrieving all accounts");

        List<Account> accounts = accountRepository.findAll();

        logger.info("Found {} total accounts", accounts.size());

        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get account by ID
     */
    @Transactional(readOnly = true)
    public Optional<AccountResponse> getAccountById(UUID accountId) {
        logger.info("Retrieving account with ID: {}", accountId);

        Optional<Account> account = accountRepository.findById(accountId);

        if (account.isPresent()) {
            logger.info("Account found with ID: {}", accountId);
            return Optional.of(convertToResponse(account.get()));
        } else {
            logger.warn("Account not found with ID: {}", accountId);
            return Optional.empty();
        }
    }

    /**
     * Convert Account entity to AccountResponse DTO
     */
    private AccountResponse convertToResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getClientId(),
            account.getAccType(),
            account.getAccStatus(),
            account.getOpeningDate(),
            account.getInitialDeposit(),
            account.getCurrency(),
            account.getBranchId()
        );
    }
}
