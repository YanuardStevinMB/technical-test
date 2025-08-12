package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Transaction;
import com.banco.banca.domain.entity.TransactionStatus;
import com.banco.banca.domain.entity.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionResponseDto {

    private UUID id;
    private TransactionType type;
    private Instant date;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;
    private String description;
    private String reference;
    private TransactionStatus status;
    private String createdBy;

    public static TransactionResponseDto fromEntity(Transaction transaction) {
        TransactionResponseDto response = new TransactionResponseDto();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setDate(transaction.getDate());

        if (transaction.getSourceAccount() != null) {
            response.setSourceAccountId(transaction.getSourceAccount().getId());
        }
        if (transaction.getDestinationAccount() != null) {
            response.setDestinationAccountId(transaction.getDestinationAccount().getId());
        }

        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setReference(transaction.getReference());
        response.setStatus(transaction.getStatus());
        response.setCreatedBy(transaction.getCreatedBy());
        return response;
    }
}
