package com.banco.banca.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawRequestDto {

    @NotNull
    private UUID sourceAccountId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
