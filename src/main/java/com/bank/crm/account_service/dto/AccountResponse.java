package com.bank.crm.account_service.dto;

import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.model.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private UUID id;
    private UUID clientId;
    private AccountType accType;
    private AccountStatus accStatus;
    private LocalDateTime openingDate;
    private BigDecimal initialDeposit;
    private String currency;
    private Integer branchId;
}
