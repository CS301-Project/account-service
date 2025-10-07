package com.bank.crm.account_service.dto;

import com.bank.crm.account_service.model.AccountType;
import com.bank.crm.account_service.model.AccountStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
}
