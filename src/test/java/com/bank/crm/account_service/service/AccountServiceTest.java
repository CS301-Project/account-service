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
        String userId = "test-agent-123";
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

        AccountResponse response = accountService.createAccount(request, userId);

        assertNotNull(response);
        assertEquals(savedAccount.getId(), response.getId());
        verify(accountRepository).save(any(Account.class));
        verify(loggingService).sendCreateLog(eq(userId), eq(request.getClientId().toString()), anyString());
    }

    @Test
    void deleteAccount_shouldDeleteIfExists() {
        String userId = "test-agent-456";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account account = new Account(
                clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
                LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        account.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountId, userId);

        verify(accountRepository).deleteById(accountId);
        verify(loggingService).sendDeleteLog(eq(userId), eq(clientId.toString()), anyString());
    }

    @Test
    void deleteAccount_shouldThrowIfNotExists() {
        String userId = "test-agent-789";
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(accountId, userId));

        verify(accountRepository, never()).deleteById(any());
        verify(loggingService, never()).sendDeleteLog(anyString(), anyString(), anyString());
    }

    @Test
    void getAccountsByClientId_shouldReturnList() {
        String userId = "test-agent-111";
        UUID clientId = UUID.randomUUID();
        List<Account> accounts = List.of(
                new Account(clientId, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1)
        );
        when(accountRepository.findByClientId(clientId)).thenReturn(accounts);

        List<AccountResponse> responses = accountService.getAccountsByClientId(clientId, userId);

        assertEquals(1, responses.size());
        verify(accountRepository).findByClientId(clientId);
        verify(loggingService).sendReadLog(eq(userId), eq(clientId.toString()), anyString());
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
        String userId = "test-agent-222";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account account = new Account(clientId, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1);
        account.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<AccountResponse> response = accountService.getAccountById(accountId, userId);

        assertTrue(response.isPresent());
        assertEquals(accountId, response.get().getId());
        verify(accountRepository).findById(accountId);
        verify(loggingService).sendReadLog(eq(userId), eq(clientId.toString()), anyString());
    }

    @Test
    void getAccountById_shouldReturnEmptyIfNotExists() {
        String userId = "test-agent-333";
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        Optional<AccountResponse> response = accountService.getAccountById(accountId, userId);

        assertFalse(response.isPresent());
        verify(accountRepository).findById(accountId);
        verify(loggingService, never()).sendReadLog(anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldThrowIfNotExists() {
        String userId = "test-agent-444";
        UUID accountId = UUID.randomUUID();
        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, AccountStatus.ACTIVE, BigDecimal.valueOf(2000.0), "EUR", 2
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(accountId, request, userId));
        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any(Account.class));
        verify(loggingService, never()).sendUpdateLog(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateAllFields() {
        String userId = "test-agent-555";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, AccountStatus.INACTIVE, BigDecimal.valueOf(2000.0), "EUR", 2
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals(AccountType.CHECKING, existingAccount.getAccType());
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccStatus());
        assertEquals(BigDecimal.valueOf(2000.0), existingAccount.getInitialDeposit());
        assertEquals("EUR", existingAccount.getCurrency());
        assertEquals(2, existingAccount.getBranchId());
        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(existingAccount);
        // Should call sendUpdateLog 5 times (once for each field)
        verify(loggingService, times(5)).sendUpdateLog(eq(userId), eq(clientId.toString()), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateOnlyAccType() {
        String userId = "test-agent-666";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                AccountType.CHECKING, null, null, null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals(AccountType.CHECKING, existingAccount.getAccType());
        assertEquals(AccountStatus.ACTIVE, existingAccount.getAccStatus()); // unchanged
        verify(accountRepository).save(existingAccount);
        verify(loggingService, times(1)).sendUpdateLog(eq(userId), eq(clientId.toString()), eq("Account Type"), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateOnlyAccStatus() {
        String userId = "test-agent-777";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, AccountStatus.INACTIVE, null, null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals(AccountStatus.INACTIVE, existingAccount.getAccStatus());
        assertEquals(AccountType.SAVINGS, existingAccount.getAccType()); // unchanged
        verify(accountRepository).save(existingAccount);
        verify(loggingService, times(1)).sendUpdateLog(eq(userId), eq(clientId.toString()), eq("Account Status"), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateOnlyInitialDeposit() {
        String userId = "test-agent-888";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, BigDecimal.valueOf(5000.0), null, null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(5000.0), existingAccount.getInitialDeposit());
        assertEquals("USD", existingAccount.getCurrency()); // unchanged
        verify(accountRepository).save(existingAccount);
        verify(loggingService, times(1)).sendUpdateLog(eq(userId), eq(clientId.toString()), eq("Initial Deposit"), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateOnlyCurrency() {
        String userId = "test-agent-999";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, null, "GBP", null
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals("GBP", existingAccount.getCurrency());
        assertEquals(1, existingAccount.getBranchId()); // unchanged
        verify(accountRepository).save(existingAccount);
        verify(loggingService, times(1)).sendUpdateLog(eq(userId), eq(clientId.toString()), eq("Currency"), anyString(), anyString(), anyString());
    }

    @Test
    void updateAccount_shouldUpdateOnlyBranchId() {
        String userId = "test-agent-000";
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Account existingAccount = new Account(
            clientId, AccountType.SAVINGS, AccountStatus.ACTIVE,
            LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1
        );
        existingAccount.setId(accountId);

        com.bank.crm.account_service.dto.UpdateAccountRequest request =
            new com.bank.crm.account_service.dto.UpdateAccountRequest(
                null, null, null, null, 5
            );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        AccountResponse response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals(5, existingAccount.getBranchId());
        assertEquals(AccountType.SAVINGS, existingAccount.getAccType()); // unchanged
        verify(accountRepository).save(existingAccount);
        verify(loggingService, times(1)).sendUpdateLog(eq(userId), eq(clientId.toString()), eq("Branch ID"), anyString(), anyString(), anyString());
    }
}
