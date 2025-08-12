package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Movement;
import com.banco.banca.domain.entity.MovementType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class MovementResponseDto {

    private UUID id;
    private UUID accountId;
    private MovementType movementType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Instant date;

    public static MovementResponseDto fromEntity(Movement movement) {
        MovementResponseDto r = new MovementResponseDto();
        r.setId(movement.getId());
        r.setAccountId(movement.getAccount() != null ? movement.getAccount().getId() : null);
        r.setMovementType(movement.getMovementType());
        r.setAmount(movement.getAmount());
        r.setBalanceBefore(movement.getBalanceBefore());
        r.setBalanceAfter(movement.getBalanceAfter());
        r.setDate(movement.getDate());
        return r;
    }
}
