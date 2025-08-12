package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Account;
import com.banco.banca.domain.entity.AccountStatus;
import com.banco.banca.domain.entity.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class AccountResponseDto {

    private UUID id;
    private UUID clientId;
    private AccountType accountType;
    private String accountNumber;
    private AccountStatus status;
    private BigDecimal balance;
    private boolean gmfExempt;
    private String ownerUser;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public static AccountResponseDto fromEntity(Account account) {
        AccountResponseDto response = new AccountResponseDto();
        response.setId(account.getId());
        response.setClientId(account.getClient() != null ? account.getClient().getId() : null);
        response.setAccountType(account.getAccountType());
        response.setAccountNumber(account.getAccountNumber());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setGmfExempt(account.isGmfExempt());
        response.setOwnerUser(account.getOwnerUser());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setVersion(account.getVersion());
        return response;
    }
}
