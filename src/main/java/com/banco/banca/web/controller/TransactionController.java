package com.banco.banca.web.controller;


import com.banco.banca.domain.service.TransactionService;
import com.banco.banca.web.dto.ConsignmentRequestDto;
import com.banco.banca.web.dto.TransactionResponseDto;
import com.banco.banca.web.dto.TransferRequestDto;
import com.banco.banca.web.dto.WithdrawRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Operaciones de consignación, retiro y transferencia")
public class TransactionController {

    private final TransactionService transactionService;

    private String currentUser(String header) {
        return header != null && !header.isBlank() ? header : "system";
    }

    @PostMapping("/deposit")
    @Operation(summary = "Consignar a una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransactionResponseDto> deposit(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value = "X-User", required = false) String user,
            @Valid @RequestBody ConsignmentRequestDto req) {
        TransactionResponseDto tx = transactionService.deposit(req.getDestinationAccountId(), req.getAmount(), req.getDescription(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Retirar de una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransactionResponseDto> withdraw(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value = "X-User", required = false) String user,
            @Valid @RequestBody WithdrawRequestDto req) {
        TransactionResponseDto tx = transactionService.withdraw(req.getSourceAccountId(), req.getAmount(), req.getDescription(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transferir entre cuentas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Reglas de negocio impiden la operación")
    })
    public ResponseEntity<TransactionResponseDto> transfer(
            @Parameter(in = ParameterIn.HEADER, name = "X-User", description = "Usuario que ejecuta la operación", required = false)
            @RequestHeader(value = "X-User", required = false) String user,
            @Valid @RequestBody TransferRequestDto req) {
        TransactionResponseDto tx = transactionService.transfer(req.getSourceAccountId(), req.getDestinationAccountId(), req.getAmount(), req.getDescription(), currentUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping
    @Operation(summary = "Listar transacciones", description = "Filtra por cuenta y rango de fechas en formato ISO-8601")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de transacciones")
    })
    public List<TransactionResponseDto> list(
            @RequestParam(name = "account", required = false) UUID account,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
    ) {
        return transactionService.search(account, fromDate, toDate);
    }
}
