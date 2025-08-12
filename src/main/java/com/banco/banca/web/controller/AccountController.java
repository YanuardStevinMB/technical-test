package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Account;
import com.banco.banca.domain.entity.AccountStatus;
import com.banco.banca.domain.entity.AccountType;
import com.banco.banca.domain.service.AccountService;
import com.banco.banca.domain.service.Interface.IAccountService;
import com.banco.banca.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
public class AccountController {

    private final IAccountService accountService;

    @PostMapping
    @Operation(summary = "Crear cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> create(@Valid @RequestBody AccountCreateRequestDto request) {
        Account created = accountService.create(
                request.getClientId(),
                request.getAccountType(),
                request.getGmfExempt(),
                request.getOwnerUser()
        );
        AccountResponseDto responseData = AccountResponseDto.fromEntity(created);

        ApiResponseDto<AccountResponseDto> response = new ApiResponseDto<>(
                "Cuenta creada correctamente",
                responseData,
                true
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-balance")
    @Operation(summary = "Crear cuenta con saldo inicial")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente")
    })
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> createWithBalance(@Valid @RequestBody AccountCreateWithBalanceRequestDto request) {
        Account created = accountService.createWithBalance(
                request.getClientId(),
                request.getAccountType(),
                request.getInitialBalance(),
                request.getGmfExempt(),
                request.getOwnerUser()
        );

        AccountResponseDto responseData = AccountResponseDto.fromEntity(created);

        ApiResponseDto<AccountResponseDto> response = new ApiResponseDto<>(
                "Cuenta creada exitosamente con saldo inicial",
                responseData,
                true
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Filtra por cliente, tipo, estado y número de cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de cuentas")
    })
    public ResponseEntity<ApiResponseDto<List<AccountResponseDto>>> list(
            @RequestParam(name = "clientId", required = false) UUID clientId,
            @RequestParam(name = "accountType", required = false) AccountType accountType,
            @RequestParam(name = "status", required = false) AccountStatus status,
            @RequestParam(name = "accountNumber", required = false) String accountNumber
    ) {
        List<AccountResponseDto> accounts = accountService.search(clientId, accountType, status, accountNumber)
                .stream()
                .map(AccountResponseDto::fromEntity)
                .toList();

        ApiResponseDto<List<AccountResponseDto>> response = new ApiResponseDto<>(
                "Listado de cuentas",
                accounts,
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponseDto<AccountDetailResponseDto>> get(@PathVariable(name = "id") UUID id) {
        try {
            Account account = accountService.get(id);
            var movements = accountService.lastMovements(id, 10).stream()
                    .map(MovementResponseDto::fromEntity)
                    .toList();

            AccountDetailResponseDto detailDto = AccountDetailResponseDto.fromEntity(account, movements);

            ApiResponseDto<AccountDetailResponseDto> response = new ApiResponseDto<>(
                    "Cuenta encontrada",
                    detailDto,
                    true
            );

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException ex) {
            ApiResponseDto<AccountDetailResponseDto> response = new ApiResponseDto<>(
                    "Cuenta no encontrada",
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> updateStatus(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody AccountUpdateStatusRequestDto request) {

        try {
            Account updated = accountService.updateStatus(id, request.getStatus());

            ApiResponseDto<AccountResponseDto> response = new ApiResponseDto<>(
                    "Cuenta actualizada exitosamente",
                    AccountResponseDto.fromEntity(updated),
                    true
            );

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException ex) {
            ApiResponseDto<AccountResponseDto> response = new ApiResponseDto<>(
                    "Cuenta no encontrada",
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException ex) {
            ApiResponseDto<AccountResponseDto> response = new ApiResponseDto<>(
                    "Solicitud inválida: " + ex.getMessage(),
                    null,
                    false
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

}
