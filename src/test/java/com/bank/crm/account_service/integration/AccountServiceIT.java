package com.bank.crm.account_service.integration;

import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.dto.UpdateAccountRequest;
import com.bank.crm.account_service.model.Account;
import com.bank.crm.account_service.model.AccountStatus;
import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.bank.crm.account_service.integration.TestDataFactory.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@Testcontainers
@Import(TestContainerConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(properties = "spring.profiles.active=test")
class AccountServiceIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    // CREATE ACCOUNT TESTS

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId", is(newAccount.getClientId().toString())))
                .andExpect(jsonPath("$.accType", is(newAccount.getAccType().toString())))
                .andExpect(jsonPath("$.accStatus", is(newAccount.getAccStatus().toString())))
                .andExpect(jsonPath("$.initialDeposit", is(newAccount.getInitialDeposit().doubleValue())))
                .andExpect(jsonPath("$.currency", is(newAccount.getCurrency())))
                .andExpect(jsonPath("$.branchId", is(newAccount.getBranchId())));

        assertEquals(1, accountRepository.count());
    }

    @Test
    void shouldFailWhenClientIdIsNull() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();
        newAccount.setClientId(null);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Client ID is required")));
    }

    @Test
    void shouldFailWhenAccountTypeIsNull() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();
        newAccount.setAccType(null);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Account type is required")));
    }

    @Test
    void shouldFailWhenInitialDepositIsNegative() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();
        newAccount.setInitialDeposit(new BigDecimal("-100.00"));

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Initial deposit must be positive")));
    }

    @Test
    void shouldFailWhenCurrencyIsInvalid() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();
        newAccount.setCurrency("US"); // Too short

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Currency must be 3 characters")));
    }

    @Test
    void shouldFailWhenBranchIdIsNegative() throws Exception {
        CreateAccountRequest newAccount = validCreateAccountRequest();
        newAccount.setBranchId(-1);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Branch ID must be positive")));
    }

    // GET ACCOUNT TESTS

    @Test
    void shouldGetAccountByIdSuccessfully() throws Exception {
        Account existingAccount = validAccount();
        accountRepository.saveAndFlush(existingAccount);

        mvc.perform(get("/api/accounts/" + existingAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingAccount.getId().toString())))
                .andExpect(jsonPath("$.clientId", is(existingAccount.getClientId().toString())))
                .andExpect(jsonPath("$.accType", is(existingAccount.getAccType().toString())))
                .andExpect(jsonPath("$.accStatus", is(existingAccount.getAccStatus().toString())));
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mvc.perform(get("/api/accounts/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAccountsByClientIdSuccessfully() throws Exception {
        UUID clientId = UUID.randomUUID();
        Account account1 = accountWithClientId(clientId);
        Account account2 = accountWithClientId(clientId);
        accountRepository.saveAndFlush(account1);
        accountRepository.saveAndFlush(account2);

        mvc.perform(get("/api/accounts/client/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientId", is(clientId.toString())))
                .andExpect(jsonPath("$[1].clientId", is(clientId.toString())));
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsForClient() throws Exception {
        UUID clientId = UUID.randomUUID();

        mvc.perform(get("/api/accounts/client/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetAllAccountsSuccessfully() throws Exception {
        Account account1 = validAccount();
        Account account2 = validAccount();
        accountRepository.saveAndFlush(account1);
        accountRepository.saveAndFlush(account2);

        mvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // UPDATE ACCOUNT TESTS

    @Test
    void shouldUpdateAccountSuccessfully() throws Exception {
        Account existingAccount = validAccount();
        accountRepository.saveAndFlush(existingAccount);

        UpdateAccountRequest updateRequest = validUpdateAccountRequest();

        mvc.perform(put("/api/accounts/" + existingAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingAccount.getId().toString())))
                .andExpect(jsonPath("$.accType", is(updateRequest.getAccType().toString())))
                .andExpect(jsonPath("$.accStatus", is(updateRequest.getAccStatus().toString())))
                .andExpect(jsonPath("$.initialDeposit", is(updateRequest.getInitialDeposit().doubleValue())))
                .andExpect(jsonPath("$.currency", is(updateRequest.getCurrency())))
                .andExpect(jsonPath("$.branchId", is(updateRequest.getBranchId())));

        Account updatedAccount = accountRepository.findById(existingAccount.getId()).orElseThrow();
        assertEquals(updateRequest.getAccType(), updatedAccount.getAccType());
        assertEquals(updateRequest.getAccStatus(), updatedAccount.getAccStatus());
        assertEquals(updateRequest.getInitialDeposit(), updatedAccount.getInitialDeposit());
        assertEquals(updateRequest.getCurrency(), updatedAccount.getCurrency());
        assertEquals(updateRequest.getBranchId(), updatedAccount.getBranchId());
    }

    @Test
    void shouldIgnoreNullFieldsDuringUpdate() throws Exception {
        Account existingAccount = validAccount();
        accountRepository.saveAndFlush(existingAccount);

        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setAccType(AccountType.CHECKING); // Only update this field

        mvc.perform(put("/api/accounts/" + existingAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accType", is(AccountType.CHECKING.toString())))
                .andExpect(jsonPath("$.accStatus", is(existingAccount.getAccStatus().toString())))
                .andExpect(jsonPath("$.currency", is(existingAccount.getCurrency())));
    }

    @Test
    void shouldFailUpdateWhenAccountNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UpdateAccountRequest updateRequest = validUpdateAccountRequest();

        mvc.perform(put("/api/accounts/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailUpdateWhenInitialDepositIsNegative() throws Exception {
        Account existingAccount = validAccount();
        accountRepository.saveAndFlush(existingAccount);

        UpdateAccountRequest updateRequest = validUpdateAccountRequest();
        updateRequest.setInitialDeposit(new BigDecimal("-50.00"));

        mvc.perform(put("/api/accounts/" + existingAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Initial deposit must be positive")));
    }

    // DELETE ACCOUNT TESTS

    @Test
    void shouldDeleteAccountSuccessfully() throws Exception {
        Account existingAccount = validAccount();
        accountRepository.saveAndFlush(existingAccount);

        mvc.perform(delete("/api/accounts/" + existingAccount.getId()))
                .andExpect(status().isNoContent());

        assertFalse(accountRepository.existsById(existingAccount.getId()));
    }

    @Test
    void shouldFailDeleteWhenAccountNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mvc.perform(delete("/api/accounts/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    // VALIDATION TESTS

    @Test
    void shouldFailWhenInvalidUUIDInPath() throws Exception {
        mvc.perform(get("/api/accounts/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleMultipleAccountsForSameClient() throws Exception {
        UUID clientId = UUID.randomUUID();

        // Create multiple accounts for the same client
        CreateAccountRequest request1 = createAccountRequestWithClientId(clientId);
        request1.setAccType(AccountType.SAVINGS);

        CreateAccountRequest request2 = createAccountRequestWithClientId(clientId);
        request2.setAccType(AccountType.CHECKING);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Verify both accounts exist for the client
        mvc.perform(get("/api/accounts/client/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
