package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.domain.entity.Client;
import com.banco.banca.domain.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client validClient;

    @BeforeEach
    void setUp() {
        validClient = Client.builder()
                .id(UUID.randomUUID())
                .identificationType("CC")
                .identificationNumber("12345678")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .birthDate(LocalDate.now().minusYears(25))
                .build();
    }

    @Test
    void createClient_WithValidData_ShouldCreateSuccessfully() {
        // Arrange
        when(clientRepository.existsByIdentificationTypeAndIdentificationNumber(any(), any()))
                .thenReturn(false);
        when(clientRepository.save(any())).thenReturn(validClient);

        // Act
        Client result = clientService.create(validClient);

        // Assert
        assertNotNull(result);
        verify(clientRepository).save(validClient);
    }

    @Test
    void createClient_Underage_ShouldThrowException() {
        // Arrange

        validClient.setBirthDate(LocalDate.now().minusYears(17));


        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> clientService.create(validClient));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot create a client under 18 years old", exception.getMessage());
    }

    @Test
    void createClient_DuplicateIdentification_ShouldThrowException() {
        // Arrange
        when(clientRepository.existsByIdentificationTypeAndIdentificationNumber(any(), any()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> clientService.create(validClient));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("A client with the same type and identification number already exists", exception.getMessage());
    }

    @Test
    void createClient_ShortFirstName_ShouldThrowException() {
        // Arrange
        validClient.setFirstName("A");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> clientService.create(validClient));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("First name must have at least 2 characters", exception.getMessage());
    }

    @Test
    void createClient_ShortLastName_ShouldThrowException() {
        // Arrange
        validClient.setLastName("B");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> clientService.create(validClient));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Last name must have at least 2 characters", exception.getMessage());
    }

    @Test
    void createClient_InvalidEmail_ShouldThrowException() {
        // Arrange
        validClient.setEmail("invalid-email");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> clientService.create(validClient));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void createClient_NullEmail_ShouldCreateSuccessfully() {
        // Arrange
        validClient.setEmail(null);
        when(clientRepository.existsByIdentificationTypeAndIdentificationNumber(any(), any()))
                .thenReturn(false);
        when(clientRepository.save(any())).thenReturn(validClient);

        // Act
        Client result = clientService.create(validClient);

        // Assert
        assertNotNull(result);
        verify(clientRepository).save(validClient);
    }

    @Test
    void createClient_EmptyEmail_ShouldCreateSuccessfully() {
        // Arrange
        validClient.setEmail("");
        when(clientRepository.existsByIdentificationTypeAndIdentificationNumber(any(), any()))
                .thenReturn(false);
        when(clientRepository.save(any())).thenReturn(validClient);

        // Act
        Client result = clientService.create(validClient);

        // Assert
        assertNotNull(result);
        verify(clientRepository).save(validClient);
    }
}
