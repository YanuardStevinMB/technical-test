package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AccountCreateRequestDto {

    @NotNull
    private UUID clientId;

    @NotNull
    private AccountType accountType;

    @NotNull(message = "gmfExempt is required")
    private Boolean gmfExempt;

    private String ownerUser;
}
