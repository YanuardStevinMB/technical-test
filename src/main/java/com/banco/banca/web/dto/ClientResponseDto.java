package com.banco.banca.web.dto;

import com.banco.banca.domain.entity.Client;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
public class ClientResponseDto {

    private UUID id;
    private String identificationType;
    private String identificationNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public static ClientResponseDto fromEntity(Client client) {
        ClientResponseDto response = new ClientResponseDto();
        response.setId(client.getId());
        response.setIdentificationType(client.getIdentificationType());
        response.setIdentificationNumber(client.getIdentificationNumber());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setEmail(client.getEmail());
        response.setBirthDate(client.getBirthDate());
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());
        response.setVersion(client.getVersion());
        return response;
    }
}
