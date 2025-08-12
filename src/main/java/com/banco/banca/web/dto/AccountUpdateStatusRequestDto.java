package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUpdateStatusRequestDto {
    @NotNull
    private AccountStatus status;
}

