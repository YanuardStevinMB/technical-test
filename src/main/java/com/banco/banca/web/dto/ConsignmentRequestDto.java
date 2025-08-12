package com.banco.banca.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ConsignmentRequestDto {

    @NotNull(message = "El ID de la cuenta destino es requerido")
    private UUID destinationAccountId;

    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    private String description;
}
