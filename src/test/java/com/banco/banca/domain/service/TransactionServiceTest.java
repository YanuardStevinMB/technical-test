package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.AccountRepository;
import com.banco.banca.domain.repository.MovementRepository;
import com.banco.banca.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID accountAId;
    private Account accountA;

    @BeforeEach
    void setup() {
        accountAId = UUID.randomUUID();
        accountA = Account.builder()
                .id(accountAId)
                .accountType(AccountType.AHORROS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();
    }

    @Test
    void withdraw_SavingsAccount_InsufficientFunds_ShouldThrowConflict() {
        when(accountRepository.findWithLockingById(accountAId)).thenReturn(Optional.of(accountA));
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transactionService.withdraw(accountAId, new BigDecimal("200"), "test", "user")
        );
        assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    @Test
    void transfer_SameAccount_ShouldThrowBadRequest() {
        UUID id = UUID.randomUUID();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transactionService.transfer(id, id, new BigDecimal("10"), "test", "user")
        );
        assertTrue(ex.getMessage().contains("cannot be the same"));
    }

    @Test
    void deposit_Success_ShouldCreateTransactionAndMovement() {
        when(accountRepository.findWithLockingById(accountAId)).thenReturn(Optional.of(accountA));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> {
            Movement m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        var resp = transactionService.deposit(accountAId, new BigDecimal("50"), "deposit", "user");
        assertNotNull(resp.getId());
        assertEquals(TransactionType.CONSIGNACION, resp.getType());
        assertEquals(new BigDecimal("50"), resp.getAmount());
    }
}
