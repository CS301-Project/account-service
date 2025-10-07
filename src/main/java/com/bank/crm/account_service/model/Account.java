package com.bank.crm.account_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private UUID id;

    @Column(name = "client_id", nullable = false)
    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "account_type", nullable = false, columnDefinition = "account_type")
    @NotNull(message = "Account type is required")
    private AccountType accType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "account_status", nullable = false, columnDefinition = "account_status")
    @NotNull(message = "Account status is required")
    private AccountStatus accStatus;

    @Column(name = "opening_date", nullable = false)
    @NotNull(message = "Opening date is required")
    private LocalDateTime openingDate;

    @Column(name = "initial_deposit", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Column(name = "branch_id", nullable = false)
    @NotNull(message = "Branch ID is required")
    @Positive(message = "Branch ID must be positive")
    private Integer branchId;
}
