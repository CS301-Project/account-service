package com.bank.crm.account_service.dto;

import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.model.AccountStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateAccountRequest {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Account type is required")
    private AccountType accType;

    @NotNull(message = "Account status is required")
    private AccountStatus accStatus;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Branch ID is required")
    @Positive(message = "Branch ID must be positive")
    private Integer branchId;

    // Constructors
    public CreateAccountRequest() {}

    public CreateAccountRequest(UUID clientId, AccountType accType, AccountStatus accStatus,
                               BigDecimal initialDeposit, String currency, Integer branchId) {
        this.clientId = clientId;
        this.accType = accType;
        this.accStatus = accStatus;
        this.initialDeposit = initialDeposit;
        this.currency = currency;
        this.branchId = branchId;
    }

    // Getters and Setters
    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public AccountType getAccType() {
        return accType;
    }

    public void setAccType(AccountType accType) {
        this.accType = accType;
    }

    public AccountStatus getAccStatus() {
        return accStatus;
    }

    public void setAccStatus(AccountStatus accStatus) {
        this.accStatus = accStatus;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }
}
