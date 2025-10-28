package com.bank.crm.account_service.controller;

import com.bank.crm.account_service.dto.AccountResponse;
import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.dto.UpdateAccountRequest;
import com.bank.crm.account_service.exception.AccountNotFoundException;
import com.bank.crm.account_service.model.AccountStatus;
import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@WithMockUser
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private UUID testAccountId;
    private UUID testClientId;
    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testClientId = UUID.randomUUID();

        createRequest = new CreateAccountRequest(
                testClientId,
                AccountType.SAVINGS,
                AccountStatus.ACTIVE,
                new BigDecimal("1000.00"),
                "USD",
                1
        );

        updateRequest = new UpdateAccountRequest(
                AccountType.CHECKING,
                AccountStatus.ACTIVE,
                new BigDecimal("2000.00"),
                "USD",
                2
        );

        accountResponse = new AccountResponse(
                testAccountId,
                testClientId,
                AccountType.SAVINGS,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                new BigDecimal("1000.00"),
                "USD",
                1
        );
    }

//    @Test
//    void createAccount_Success() throws Exception {
//        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
//                .thenReturn(accountResponse);
//
//        mockMvc.perform(post("/api/accounts")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(testAccountId.toString()))
//                .andExpect(jsonPath("$.clientId").value(testClientId.toString()))
//                .andExpect(jsonPath("$.accType").value("SAVINGS"))
//                .andExpect(jsonPath("$.accStatus").value("ACTIVE"))
//                .andExpect(jsonPath("$.initialDeposit").value(1000.00))
//                .andExpect(jsonPath("$.currency").value("USD"))
//                .andExpect(jsonPath("$.branchId").value(1));
//
//        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), anyString());
//    }

    @Test
    void createAccount_Success() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testAccountId.toString()))
                .andExpect(jsonPath("$.clientId").value(testClientId.toString()))
                .andExpect(jsonPath("$.accType").value("SAVINGS"))
                .andExpect(jsonPath("$.accStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.initialDeposit").value(1000.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.branchId").value(1));

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), anyString());
    }


    @Test
    void createAccount_ValidationFails_NullClientId() throws Exception {
        CreateAccountRequest invalidRequest = new CreateAccountRequest(
                null,
                AccountType.SAVINGS,
                AccountStatus.ACTIVE,
                new BigDecimal("1000.00"),
                "USD",
                1
        );

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class), anyString());
    }

    @Test
    void createAccount_ValidationFails_NegativeDeposit() throws Exception {
        CreateAccountRequest invalidRequest = new CreateAccountRequest(
                testClientId,
                AccountType.SAVINGS,
                AccountStatus.ACTIVE,
                new BigDecimal("-100.00"),
                "USD",
                1
        );

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class), anyString());
    }

    @Test
    void createAccount_ValidationFails_InvalidCurrency() throws Exception {
        CreateAccountRequest invalidRequest = new CreateAccountRequest(
                testClientId,
                AccountType.SAVINGS,
                AccountStatus.ACTIVE,
                new BigDecimal("1000.00"),
                "US",
                1
        );

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class), anyString());
    }

    @Test
    void deleteAccount_Success() throws Exception {
        doNothing().when(accountService).deleteAccount(eq(testAccountId), anyString());

        mockMvc.perform(delete("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123"))

                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(eq(testAccountId), anyString());
    }

    @Test
    void deleteAccount_NotFound() throws Exception {
        doThrow(new AccountNotFoundException("Account not found with ID: " + testAccountId))
                .when(accountService).deleteAccount(eq(testAccountId), anyString());

        mockMvc.perform(delete("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).deleteAccount(eq(testAccountId), anyString());
    }

    @Test
    void getAccountsByClientId_Success() throws Exception {
        List<AccountResponse> accounts = Arrays.asList(accountResponse);
        when(accountService.getAccountsByClientId(eq(testClientId), anyString())).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts/client/{clientId}", testClientId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testAccountId.toString()))
                .andExpect(jsonPath("$[0].clientId").value(testClientId.toString()));

        verify(accountService, times(1)).getAccountsByClientId(eq(testClientId), anyString());
    }


    @Test
    void getAccountsByClientId_EmptyList() throws Exception {
        when(accountService.getAccountsByClientId(eq(testClientId), anyString())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/accounts/client/{clientId}", testClientId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(accountService, times(1)).getAccountsByClientId(eq(testClientId), anyString());
    }

    @Test
    void getAllAccounts_Success() throws Exception {
        List<AccountResponse> accounts = Arrays.asList(accountResponse);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testAccountId.toString()));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAllAccounts_EmptyList() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAccountById_Success() throws Exception {
        when(accountService.getAccountById(eq(testAccountId), anyString())).thenReturn(Optional.of(accountResponse));

        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccountId.toString()))
                .andExpect(jsonPath("$.clientId").value(testClientId.toString()))
                .andExpect(jsonPath("$.accType").value("SAVINGS"))
                .andExpect(jsonPath("$.accStatus").value("ACTIVE"));

        verify(accountService, times(1)).getAccountById(eq(testAccountId), anyString());
    }

    @Test
    void getAccountById_NotFound() throws Exception {
        when(accountService.getAccountById(eq(testAccountId), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountById(eq(testAccountId), anyString());
    }

    @Test
    void updateAccount_Success() throws Exception {
        AccountResponse updatedResponse = new AccountResponse(
                testAccountId,
                testClientId,
                AccountType.CHECKING,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                new BigDecimal("2000.00"),
                "USD",
                2
        );

        when(accountService.updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString()))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccountId.toString()))
                .andExpect(jsonPath("$.accType").value("CHECKING"))
                .andExpect(jsonPath("$.initialDeposit").value(2000.00))
                .andExpect(jsonPath("$.branchId").value(2));

        verify(accountService, times(1)).updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString());
    }

    @Test
    void updateAccount_NotFound() throws Exception {
        when(accountService.updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString()))
                .thenThrow(new AccountNotFoundException("Account not found with ID: " + testAccountId));

        mockMvc.perform(put("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString());
    }

    @Test
    void updateAccount_ValidationFails_NegativeDeposit() throws Exception {
        UpdateAccountRequest invalidRequest = new UpdateAccountRequest(
                AccountType.CHECKING,
                AccountStatus.ACTIVE,
                new BigDecimal("-500.00"),
                "USD",
                2
        );

        mockMvc.perform(put("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString());
    }

    @Test
    void createAccount_ThrowsIllegalArgumentException() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid parameter"));

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), anyString());
    }

    @Test
    void createAccount_ThrowsRuntimeException() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), anyString());
    }

    @Test
    void deleteAccount_ThrowsRuntimeException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(accountService).deleteAccount(eq(testAccountId), anyString());

        mockMvc.perform(delete("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123"))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).deleteAccount(eq(testAccountId), anyString());
    }

    @Test
    void getAccountsByClientId_ThrowsIllegalArgumentException() throws Exception {
        when(accountService.getAccountsByClientId(eq(testClientId), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid client ID"));

        mockMvc.perform(get("/api/accounts/client/{clientId}", testClientId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isBadRequest());

        verify(accountService, times(1)).getAccountsByClientId(eq(testClientId), anyString());
    }

    @Test
    void getAccountsByClientId_ThrowsRuntimeException() throws Exception {
        when(accountService.getAccountsByClientId(eq(testClientId), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/accounts/client/{clientId}", testClientId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).getAccountsByClientId(eq(testClientId), anyString());
    }

    @Test
    void getAllAccounts_ThrowsRuntimeException() throws Exception {
        when(accountService.getAllAccounts())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAccountById_ThrowsIllegalArgumentException() throws Exception {
        when(accountService.getAccountById(eq(testAccountId), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid account ID"));

        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isBadRequest());

        verify(accountService, times(1)).getAccountById(eq(testAccountId), anyString());
    }

    @Test
    void getAccountById_ThrowsRuntimeException() throws Exception {
        when(accountService.getAccountById(eq(testAccountId), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId)
                        .param("userId", "test-user-123"))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).getAccountById(eq(testAccountId), anyString());
    }

    @Test
    void updateAccount_ThrowsIllegalArgumentException() throws Exception {
        when(accountService.updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid parameter"));

        mockMvc.perform(put("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, times(1)).updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString());
    }

    @Test
    void updateAccount_ThrowsRuntimeException() throws Exception {
        when(accountService.updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/api/accounts/{accountId}", testAccountId)
                        .with(csrf())
                        .param("userId", "test-user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(accountService, times(1)).updateAccount(eq(testAccountId), any(UpdateAccountRequest.class), anyString());
    }
}
