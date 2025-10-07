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

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(savedAccount.getId(), response.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void deleteAccount_shouldDeleteIfExists() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.existsById(accountId)).thenReturn(true);

        accountService.deleteAccount(accountId);

        verify(accountRepository).deleteById(accountId);
    }

    @Test
    void deleteAccount_shouldThrowIfNotExists() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.existsById(accountId)).thenReturn(false);

        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(accountId));
    }

    @Test
    void getAccountsByClientId_shouldReturnList() {
        UUID clientId = UUID.randomUUID();
        List<Account> accounts = List.of(
                new Account(clientId, AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDateTime.now(), BigDecimal.valueOf(1000.0), "USD", 1)
        );
        when(accountRepository.findByClientId(clientId)).thenReturn(accounts);

        List<AccountResponse> responses = accountService.getAccountsByClientId(clientId);

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

        Optional<AccountResponse> response = accountService.getAccountById(accountId);

        assertTrue(response.isPresent());
        assertEquals(accountId, response.get().getId());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_shouldReturnEmptyIfNotExists() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        Optional<AccountResponse> response = accountService.getAccountById(accountId);

        assertFalse(response.isPresent());
        verify(accountRepository).findById(accountId);
    }
}
