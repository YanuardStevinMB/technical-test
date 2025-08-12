package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.AccountRepository;
import com.banco.banca.domain.repository.MovementRepository;
import com.banco.banca.domain.repository.TransactionRepository;
import com.banco.banca.domain.service.Interface.ITransactionService;
import com.banco.banca.web.dto.TransactionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MovementRepository movementRepository;

    @Transactional
    public TransactionResponseDto deposit(UUID destinationAccountId, BigDecimal amount, String description, String user) {
        validateAmount(amount);

        var account = accountRepository.findWithLockingById(destinationAccountId)
                .orElseThrow(() -> new NotFoundException("Destination account not found"));

        validateActiveAccount(account);

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(balanceBefore.add(amount));

        Transaction tx = Transaction.builder()
                .type(TransactionType.CONSIGNACION)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.OK)
                .createdBy(user)
                .destinationAccount(account)
                .build();
        transactionRepository.save(tx);

        var movement = Movement.builder()
                .transaction(tx)
                .account(account)
                .movementType(MovementType.CREDIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(account.getBalance())
                .build();
        movementRepository.save(movement);

        accountRepository.save(account);

        return TransactionResponseDto.fromEntity(tx);
    }

    @Transactional
    public TransactionResponseDto withdraw(UUID sourceAccountId, BigDecimal amount, String description, String user) {
        validateAmount(amount);

        var account = accountRepository.findWithLockingById(sourceAccountId)
                .orElseThrow(() -> new NotFoundException("Source account not found"));

        validateActiveAccount(account);

        if (account.getAccountType() == AccountType.AHORROS && account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds in savings account", HttpStatus.CONFLICT);
        }

        if (account.getAccountType() == AccountType.CORRIENTE) {
            BigDecimal balanceAfter = account.getBalance().subtract(amount);
            if (balanceAfter.compareTo(new BigDecimal("-1000000")) < 0) {
                throw new BusinessException("Amount exceeds allowed overdraft limit", HttpStatus.CONFLICT);
            }
        }

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(balanceBefore.subtract(amount));

        Transaction tx = Transaction.builder()
                .type(TransactionType.RETIRO)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.OK)
                .createdBy(user)
                .sourceAccount(account)
                .build();
        transactionRepository.save(tx);

        var movement = Movement.builder()
                .transaction(tx)
                .account(account)
                .movementType(MovementType.DEBIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(account.getBalance())
                .build();
        movementRepository.save(movement);

        accountRepository.save(account);

        return TransactionResponseDto.fromEntity(tx);
    }

    @Transactional
    public TransactionResponseDto transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, String description, String user) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BusinessException("Source and destination accounts cannot be the same", HttpStatus.BAD_REQUEST);
        }

        validateAmount(amount);

        // Order locking to avoid deadlocks
        var firstId = Comparator.<UUID>naturalOrder().compare(sourceAccountId, destinationAccountId) <= 0 ? sourceAccountId : destinationAccountId;
        var secondId = firstId.equals(sourceAccountId) ? destinationAccountId : sourceAccountId;

        var first = accountRepository.findWithLockingById(firstId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        var second = accountRepository.findWithLockingById(secondId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        var source = first.getId().equals(sourceAccountId) ? first : second;
        var destination = source == first ? second : first;

        validateActiveAccount(source);
        validateActiveAccount(destination);

        if (source.getAccountType() == AccountType.AHORROS && source.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds in savings account", HttpStatus.CONFLICT);
        }

        if (source.getAccountType() == AccountType.CORRIENTE) {
            BigDecimal balanceAfter = source.getBalance().subtract(amount);
            if (balanceAfter.compareTo(new BigDecimal("-1000000")) < 0) {
                throw new BusinessException("Amount exceeds allowed overdraft limit", HttpStatus.CONFLICT);
            }
        }

        BigDecimal balanceBeforeSource = source.getBalance();
        BigDecimal balanceBeforeDestination = destination.getBalance();

        source.setBalance(balanceBeforeSource.subtract(amount));
        destination.setBalance(balanceBeforeDestination.add(amount));

        Transaction tx = Transaction.builder()
                .type(TransactionType.TRANSFERENCIA)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.OK)
                .createdBy(user)
                .sourceAccount(source)
                .destinationAccount(destination)
                .build();
        transactionRepository.save(tx);

        var debitMovement = Movement.builder()
                .transaction(tx)
                .account(source)
                .movementType(MovementType.DEBIT)
                .amount(amount)
                .balanceBefore(balanceBeforeSource)
                .balanceAfter(source.getBalance())
                .build();
        var creditMovement = Movement.builder()
                .transaction(tx)
                .account(destination)
                .movementType(MovementType.CREDIT)
                .amount(amount)
                .balanceBefore(balanceBeforeDestination)
                .balanceAfter(destination.getBalance())
                .build();
        movementRepository.save(debitMovement);
        movementRepository.save(creditMovement);

        accountRepository.save(source);
        accountRepository.save(destination);

        return TransactionResponseDto.fromEntity(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> search(UUID accountId, Instant fromDate, Instant toDate) {
        Specification<Transaction> spec = Specification.where(null);

        if (accountId != null) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("sourceAccount").get("id"), accountId),
                    cb.equal(root.get("destinationAccount").get("id"), accountId)
            ));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), toDate));
        }

        var list = transactionRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date"));

        return list.stream()
                .map(TransactionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("Amount is required", HttpStatus.BAD_REQUEST);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
        }
        if (amount.compareTo(new BigDecimal("999999999999.99")) > 0) {
            throw new BusinessException("Amount exceeds the maximum allowed limit", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account must be active to perform transactions", HttpStatus.CONFLICT);
        }
    }
}
