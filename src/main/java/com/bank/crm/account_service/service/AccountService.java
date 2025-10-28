package com.bank.crm.account_service.service;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.dto.UpdateAccountRequest;
import com.bank.crm.account_service.exception.AccountNotFoundException;
import com.bank.crm.account_service.model.Account;
import com.bank.crm.account_service.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private LoggingService loggingService;

    /**
     * Create a new account
     */
    public AccountResponse createAccount(CreateAccountRequest request, String userId) {
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

        //Create remarks for logging
        String remarks = String.format(
                "Account created with type: %s, initial deposit: %.2f %s, at branch ID: %d",
                request.getAccType(),
                request.getInitialDeposit(),
                request.getCurrency(),
                request.getBranchId()
        );

        loggingService.sendCreateLog(userId, request.getClientId().toString(), remarks);

        return convertToResponse(savedAccount);
    }

    /**
     * Delete an account by ID
     */
    public void deleteAccount(UUID accountId, String userId) {
        logger.info("Attempting to delete account with ID: {}", accountId);

        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            logger.warn("Account with ID {} not found for deletion", accountId);
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = optionalAccount.get();
        String clientId = account.getClientId().toString();

        accountRepository.deleteById(accountId);
        logger.info("Account with ID {} deleted successfully", accountId);

        String remarks = String.format(
                "Account for %s with ID %s deleted.",
                clientId,
                accountId
        );
        loggingService.sendDeleteLog(userId, clientId, remarks);
    }

    /**
     * Get all accounts for a specific client
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByClientId(UUID clientId, String userId) {
        logger.info("Retrieving accounts for client ID: {}", clientId);

        List<Account> accounts = accountRepository.findByClientId(clientId);

        logger.info("Found {} accounts for client ID: {}", accounts.size(), clientId);

        String remarks = String.format(
                "Retrieved %d accounts for client %s by agent %s.",
                accounts.size(),
                clientId,
                userId
        );
        loggingService.sendReadLog(userId, clientId.toString(), remarks);

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
    public Optional<AccountResponse> getAccountById(UUID accountId, String userId) {
        logger.info("Retrieving account with ID: {}", accountId);

        Optional<Account> account = accountRepository.findById(accountId);

        if (account.isPresent()) {
            logger.info("Account found with ID: {}", accountId);
            String remarks = String.format(
                    "Account for %s with ID %s accessed.",
                    account.get().getClientId().toString(),
                    accountId
            );
            loggingService.sendReadLog(userId, account.get().getClientId().toString(), remarks);
            return Optional.of(convertToResponse(account.get()));
        } else {
            logger.warn("Account not found with ID: {}", accountId);
            return Optional.empty();
        }
    }

    /**
     * Update an existing account
     */
    public AccountResponse updateAccount(UUID accountId, UpdateAccountRequest request, String userId) {
        logger.info("Updating account with ID: {}", accountId);

        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            logger.warn("Account with ID {} not found for update", accountId);
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = optionalAccount.get();
        String clientId = account.getClientId().toString();

        List<String> attributeNames = new ArrayList<>();
        List<String> beforeValues = new ArrayList<>();
        List<String> afterValues = new ArrayList<>();

        // Update only the fields that are provided (not null) and log each change
        if (request.getAccType() != null && !request.getAccType().equals(account.getAccType())) {
            String beforeValue = account.getAccType().toString();
            String afterValue = request.getAccType().toString();
            account.setAccType(request.getAccType());

            attributeNames.add("Account Type");
            beforeValues.add(beforeValue);
            afterValues.add(afterValue);
        }
        if (request.getAccStatus() != null && !request.getAccStatus().equals(account.getAccStatus())) {
            String beforeValue = account.getAccStatus().toString();
            String afterValue = request.getAccStatus().toString();
            account.setAccStatus(request.getAccStatus());

            attributeNames.add("Account Status");
            beforeValues.add(beforeValue);
            afterValues.add(afterValue);
        }
        if (request.getInitialDeposit() != null && !request.getInitialDeposit().equals(account.getInitialDeposit())) {
            String beforeValue = account.getInitialDeposit().toString();
            String afterValue = request.getInitialDeposit().toString();
            account.setInitialDeposit(request.getInitialDeposit());

            attributeNames.add("Initial Deposit");
            beforeValues.add(beforeValue);
            afterValues.add(afterValue);

        }
        if (request.getCurrency() != null && !request.getCurrency().equals(account.getCurrency())) {
            String beforeValue = account.getCurrency();
            String afterValue = request.getCurrency();
            account.setCurrency(request.getCurrency());

            attributeNames.add("Currency");
            beforeValues.add(beforeValue);
            afterValues.add(afterValue);

        }
        if (request.getBranchId() != null && !request.getBranchId().equals(account.getBranchId())) {
            String beforeValue = account.getBranchId().toString();
            String afterValue = request.getBranchId().toString();
            account.setBranchId(request.getBranchId());

            attributeNames.add("Branch ID");
            beforeValues.add(beforeValue);
            afterValues.add(afterValue);
        }

        if (!attributeNames.isEmpty()) {
            String consolidatedAttributes = String.join(" | ", attributeNames);
            String consolidatedBeforeValues = String.join(" | ", beforeValues);
            String consolidatedAfterValues = String.join(" | ", afterValues);
            String consolidatedRemarks = String.format(
                    "Updated attributes for account ID %s: %s",
                    accountId,
                    attributeNames
            );

            loggingService.sendUpdateLog(userId, clientId, consolidatedAttributes,
                    consolidatedBeforeValues, consolidatedAfterValues,
                    consolidatedRemarks);
        }

        // Save the updated account
        Account updatedAccount = accountRepository.save(account);

        logger.info("Account with ID {} updated successfully", accountId);

        return convertToResponse(updatedAccount);
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
