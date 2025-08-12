package com.banco.banca.domain.service.Interface;

import com.banco.banca.web.dto.TransactionResponseDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ITransactionService {

        TransactionResponseDto deposit(UUID destinationAccountId, BigDecimal amount, String description, String user);

        TransactionResponseDto withdraw(UUID sourceAccountId, BigDecimal amount, String description, String user);

        TransactionResponseDto transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, String description, String user);

        List<TransactionResponseDto> search(UUID accountId, Instant fromDate, Instant toDate);
}
