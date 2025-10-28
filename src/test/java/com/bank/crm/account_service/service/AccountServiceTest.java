package com.bank.crm.account_service.service;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.exception.AccountNotFoundException;
import com.bank.crm.account_service.model.Account;
import com.bank.crm.account_service.model.AccountStatus;
import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldReturnAccountResponse() {
        CreateAccountRequest request = new CreateAccountRequest(
                UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE, BigDecimal.valueOf(1000.0), "USD", 1
        );
        Account account = new Account(
                request.getClientId(), request.getAccType(), request.getAccStatus(),
                LocalDateTime.now(), request.getInitialDeposit(), request.getCurrency(), request.getBranchId()
        );
        Account savedAccount = account;
        savedAccount.setId(UUID.randomUUID());

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.createAccount(request, "test-user-123");

        assertNotNull(response);
        assertEquals(savedAccount.getId(), response.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccountsByClientId_shouldReturnList() {
        UUID clientId = UUID.randomUUID();
        List<Account> accounts = List.of(
                new Account(clientId, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1)
        );
        when(accountRepository.findByClientId(clientId)).thenReturn(accounts);

        List<AccountResponse> responses = accountService.getAccountsByClientId(clientId, "test-user-123");

        assertEquals(1, responses.size());
        verify(accountRepository).findByClientId(clientId);
    }

    @Test
    void getAllAccounts_shouldReturnList() {
        List<Account> accounts = List.of(
                new Account(UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1)
        );
        when(accountRepository.findAll()).thenReturn(accounts);

        List<AccountResponse> responses = accountService.getAllAccounts();

        assertEquals(1, responses.size());
        verify(accountRepository).findAll();
    }

    @Test
    void getAccountById_shouldReturnAccountResponseIfExists() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1);
        account.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<AccountResponse> response = accountService.getAccountById(accountId, "test-user-123");

        assertTrue(response.isPresent());
        assertEquals(accountId, response.get().getId());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_shouldReturnEmptyIfNotExists() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        Optional<AccountResponse> response = accountService.getAccountById(accountId, "test-user-123");

        assertFalse(response.isPresent());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void updateAccount_shouldThrowIfNotExists() {
        UUID accountId = UUID.randomUUID();
        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, AccountStatus.ACTIVE, BigDecimal.valueOf(2000.0), "EUR", 2
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(accountId, request, "test-user-123"));
        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccount_shouldUpdateAllFields() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, AccountStatus.INACTIVE, BigDecimal.valueOf(2000.0), "EUR", 2
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals(AccountType.CHECKING, existingAccount.getAccType());
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccStatus());
        assertEquals(BigDecimal.valueOf(2000.0), existingAccount.getInitialDeposit());
        assertEquals("EUR", existingAccount.getCurrency());
        assertEquals(2, existingAccount.getBranchId());
        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_shouldUpdateOnlyAccType() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, null, null, null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals(AccountType.CHECKING, existingAccount.getAccType());
        assertEquals(AccountStatus.ACTIVE, existingAccount.getAccStatus()); // unchanged
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_shouldUpdateOnlyAccStatus() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, AccountStatus.INACTIVE, null, null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccStatus());
        assertEquals(AccountType.SAVINGS, existingAccount.getAccType()); // unchanged
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_shouldUpdateOnlyInitialDeposit() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, BigDecimal.valueOf(5000.0), null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(5000.0), existingAccount.getInitialDeposit());
        assertEquals("USD", existingAccount.getCurrency()); // unchanged
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_shouldUpdateOnlyCurrency() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, null, "GBP", null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals("GBP", existingAccount.getCurrency());
        assertEquals(1, existingAccount.getBranchId()); // unchanged
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void updateAccount_shouldUpdateOnlyBranchId() {
        UUID accountId = UUID.randomUUID();
        Account existingAccount = new Account(
            UUID.randomUUID(), AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, null, null, 5
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, "test-user-123");

        assertNotNull(response);
        assertEquals(5, existingAccount.getBranchId());
        assertEquals(AccountType.SAVINGS, existingAccount.getAccType()); // unchanged
        verify(accountRepository).save(existingAccount);
    }
}
