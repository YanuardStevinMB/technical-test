package com.banco.banca.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRequestDto {

    @NotBlank
    private String identificationType;

    @NotBlank
    private String identificationNumber;

    @NotBlank
    @Size(min = 2)
    private String firstName;

    @NotBlank
    @Size(min = 2)
    private String lastName;

    @Email
    private String email;

    @NotNull
    private LocalDate birthDate;
}
