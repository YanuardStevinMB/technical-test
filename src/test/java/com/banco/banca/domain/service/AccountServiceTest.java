package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.ClientRepository;
import com.banco.banca.domain.repository.AccountRepository;
import com.banco.banca.domain.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID clientId;
    private Client client;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = Client.builder()
                .id(clientId)
                .firstName("John")
                .lastName("Doe")
                .identificationType("CC")
                .identificationNumber("1")
                .build();
    }

    @Test
    void create_SavingsAccount_Success_NumberWithPrefix53() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = accountService.create(clientId, AccountType.AHORROS, false, "user");

        assertNotNull(created.getAccountNumber());
        assertTrue(created.getAccountNumber().startsWith("53"));
        assertEquals(10, created.getAccountNumber().length());
        assertEquals(AccountStatus.ACTIVE, created.getStatus());
        assertEquals(BigDecimal.ZERO, created.getBalance());
    }

    @Test
    void createWithBalance_NegativeBalance_ShouldThrowBadRequest() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                accountService.createWithBalance(clientId, AccountType.AHORROS, new BigDecimal("-1"), false, "user")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    void create_ClientDoesNotExist_ShouldThrowNotFound() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.create(clientId, AccountType.AHORROS, false, null));
    }

    @Test
    void updateStatus_CancelWithNonZeroBalance_ShouldThrowConflict() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .id(accountId)
                .client(client)
                .accountType(AccountType.AHORROS)
                .accountNumber("5300000000")
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BusinessException ex = assertThrows(BusinessException.class, () -> accountService.updateStatus(accountId, AccountStatus.CANCELED));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void updateStatus_InactivateWithNegativeBalance_ShouldThrowConflict() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .id(accountId)
                .client(client)
                .accountType(AccountType.CORRIENTE)
                .accountNumber("3300000000")
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("-10"))
                .build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BusinessException ex = assertThrows(BusinessException.class, () -> accountService.updateStatus(accountId, AccountStatus.INACTIVE));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
}
