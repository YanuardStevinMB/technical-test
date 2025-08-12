package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Client;
import com.banco.banca.domain.service.ClientService;

import com.banco.banca.domain.service.Interface.IClientService;
import com.banco.banca.web.dto.ApiResponseDto;
import com.banco.banca.web.dto.ClientRequestDto;
import com.banco.banca.web.dto.ClientResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes")
public class ClientController {

    private final IClientService clientService;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Filtra por tipo/número de identificación, nombre, apellido y correo electrónico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de clientes")
    })
    public ResponseEntity<ApiResponseDto<List<ClientResponseDto>>> list(
            @RequestParam(name = "identificationType", required = false) String identificationType,
            @RequestParam(name = "identificationNumber", required = false) String identificationNumber,
            @RequestParam(name = "firstName", required = false) String firstName,
            @RequestParam(name = "lastName", required = false) String lastName,
            @RequestParam(name = "email", required = false) String email
    ) {
        var clients = clientService.search(identificationType, identificationNumber, firstName, lastName, email)
                .stream()
                .map(ClientResponseDto::fromEntity)
                .toList();

        ApiResponseDto<List<ClientResponseDto>> response = new ApiResponseDto<>(
                "Listado de clientes",
                clients,
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponseDto<ClientResponseDto>> get(@PathVariable(name = "id") UUID id) {
        try {
            Client client = clientService.get(id);
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Cliente encontrado",
                    ClientResponseDto.fromEntity(client),
                    true
            );
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Cliente no encontrado",
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    @Operation(summary = "Crear cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Conflicto de datos")
    })
    public ResponseEntity<ApiResponseDto<ClientResponseDto>> create(@Valid @RequestBody ClientRequestDto req) {
        try {
            Client client = Client.builder()
                    .identificationType(req.getIdentificationType())
                    .identificationNumber(req.getIdentificationNumber())
                    .firstName(req.getFirstName())
                    .lastName(req.getLastName())
                    .email(req.getEmail())
                    .birthDate(req.getBirthDate())
                    .build();

            Client created = clientService.create(client);

            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Cliente creado exitosamente",
                    ClientResponseDto.fromEntity(created),
                    true
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException e) {
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Conflicto de datos: " + e.getMessage(),
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Solicitud inválida: " + e.getMessage(),
                    null,
                    false
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponseDto<ClientResponseDto>> update(@PathVariable(name = "id") UUID id, @Valid @RequestBody ClientRequestDto req) {
        try {
            Client client = Client.builder()
                    .identificationType(req.getIdentificationType())
                    .identificationNumber(req.getIdentificationNumber())
                    .firstName(req.getFirstName())
                    .lastName(req.getLastName())
                    .email(req.getEmail())
                    .birthDate(req.getBirthDate())
                    .build();

            Client updated = clientService.update(id, client);

            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Cliente actualizado exitosamente",
                    ClientResponseDto.fromEntity(updated),
                    true
            );

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Cliente no encontrado",
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException e) {
            ApiResponseDto<ClientResponseDto> response = new ApiResponseDto<>(
                    "Solicitud inválida: " + e.getMessage(),
                    null,
                    false
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable(name = "id") UUID id) {
        try {
            clientService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            ApiResponseDto<Void> response = new ApiResponseDto<>(
                    "Cliente no encontrado",
                    null,
                    false
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

