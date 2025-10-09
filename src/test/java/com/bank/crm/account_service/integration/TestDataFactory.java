package com.bank.crm.account_service.integration;

import com.bank.crm.account_service.dto.CreateAccountRequest;
import com.bank.crm.account_service.dto.UpdateAccountRequest;
import com.bank.crm.account_service.model.Account;
import com.bank.crm.account_service.model.AccountStatus;
import com.bank.crm.account_service.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestDataFactory {

    public static CreateAccountRequest validCreateAccountRequest() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setClientId(UUID.randomUUID());
        request.setAccType(AccountType.SAVINGS);
        request.setAccStatus(AccountStatus.ACTIVE);
        request.setInitialDeposit(new BigDecimal("1000.00"));
        request.setCurrency("USD");
        request.setBranchId(1);
        return request;
    }

    public static CreateAccountRequest createAccountRequestWithClientId(UUID clientId) {
        CreateAccountRequest request = validCreateAccountRequest();
        request.setClientId(clientId);
        return request;
    }

    public static UpdateAccountRequest validUpdateAccountRequest() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccType(AccountType.CHECKING);
        request.setAccStatus(AccountStatus.INACTIVE);
        request.setInitialDeposit(new BigDecimal("2000.00"));
        request.setCurrency("EUR");
        request.setBranchId(2);
        return request;
    }

    public static Account validAccount() {
        return new Account(
            UUID.randomUUID(),
            AccountType.SAVINGS,
            AccountStatus.ACTIVE,
            LocalDateTime.now(),
            new BigDecimal("1000.00"),
            "USD",
            1
        );
    }

    public static Account accountWithClientId(UUID clientId) {
        return new Account(
            clientId,
            AccountType.SAVINGS,
            AccountStatus.ACTIVE,
            LocalDateTime.now(),
            new BigDecimal("1000.00"),
            "USD",
            1
        );
    }
}
