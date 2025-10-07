package com.bank.crm.account_service.dto;

import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.model.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountResponse {

    private UUID id;
    private UUID clientId;
    private AccountType accType;
    private AccountStatus accStatus;
    private LocalDateTime openingDate;
    private BigDecimal initialDeposit;
    private String currency;
    private Integer branchId;

    // Constructors
    public AccountResponse() {}

    public AccountResponse(UUID id, UUID clientId, AccountType accType, AccountStatus accStatus,
                          LocalDateTime openingDate, BigDecimal initialDeposit,
                          String currency, Integer branchId) {
        this.id = id;
        this.clientId = clientId;
        this.accType = accType;
        this.accStatus = accStatus;
        this.openingDate = openingDate;
        this.initialDeposit = initialDeposit;
        this.currency = currency;
        this.branchId = branchId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDateTime openingDate) {
        this.openingDate = openingDate;
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
