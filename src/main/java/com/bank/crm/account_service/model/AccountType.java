package com.bank.crm.account_service.model;

public enum AccountType {
    SAVINGS("Savings"),
    CHECKING("Checking"),
    INVESTMENT("Investment"),
    BUSINESS("Business");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
