package com.bank.crm.account_service.dto;

import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.model.AccountStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class UpdateAccountRequest {

    private AccountType accType;

    private AccountStatus accStatus;

    @DecimalMin(value = "0.0", inclusive = false, message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Positive(message = "Branch ID must be positive")
    private Integer branchId;

    // Constructors
    public UpdateAccountRequest() {}

    public UpdateAccountRequest(AccountType accType, AccountStatus accStatus,
                               BigDecimal initialDeposit, String currency, Integer branchId) {
        this.accType = accType;
        this.accStatus = accStatus;
        this.initialDeposit = initialDeposit;
        this.currency = currency;
        this.branchId = branchId;
    }

    // Getters and Setters
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
