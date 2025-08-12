package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountCreateWithBalanceRequestDto {

    @NotNull(message = "clientId is required")
    private UUID clientId;

    @NotNull(message = "accountType is required")
    private AccountType accountType;

    @NotNull(message = "initialBalance is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be greater than 0")
    private BigDecimal initialBalance;

    @NotNull(message = "gmfExempt is required")
    private Boolean gmfExempt;

    private String ownerUser;
}
