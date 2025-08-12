package com.banco.banca.domain.service;

import com.banco.banca.common.exception.BusinessException;
import com.banco.banca.common.exception.NotFoundException;
import com.banco.banca.domain.entity.*;
import com.banco.banca.domain.repository.AccountRepository;
import com.banco.banca.domain.repository.ClientRepository;
import com.banco.banca.domain.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final MovementRepository movementRepository;

    @Transactional
    public Account create(UUID clientId, AccountType accountType, Boolean gmfExempt, String ownerUser) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        Account account = Account.builder()
                .client(client)
                .accountType(accountType)
                .accountNumber(generateUniqueAccountNumber(accountType))
                .status(AccountStatus.ACTIVE) // initial status ACTIVE for both types
                .balance(BigDecimal.ZERO)
                .gmfExempt(gmfExempt != null ? gmfExempt : false)
                .ownerUser(ownerUser)
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public Account createWithBalance(UUID clientId, AccountType accountType, BigDecimal initialBalance,
                                     Boolean gmfExempt, String ownerUser) {
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Initial balance cannot be negative", HttpStatus.BAD_REQUEST);
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        Account account = Account.builder()
                .client(client)
                .accountType(accountType)
                .accountNumber(generateUniqueAccountNumber(accountType))
                .status(AccountStatus.ACTIVE)
                .balance(initialBalance)
                .gmfExempt(gmfExempt != null ? gmfExempt : false)
                .ownerUser(ownerUser)
                .build();

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @Transactional
    public Account updateStatus(UUID id, AccountStatus newStatus) {
        Account account = get(id);

        if (newStatus == AccountStatus.CANCELED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Only accounts with zero balance can be canceled", HttpStatus.CONFLICT);
        }

        if (newStatus == AccountStatus.INACTIVE && account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Accounts with negative balance cannot be set to INACTIVE", HttpStatus.CONFLICT);
        }

        account.setStatus(newStatus);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Movement> lastMovements(UUID accountId, int limit) {
        // Currently supports top 10; adapt repository if you need dynamic limits
        return movementRepository.findTop10ByAccount_IdOrderByDateDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Account> search(UUID clientId, AccountType accountType, AccountStatus status, String accountNumber) {
        return accountRepository.search(clientId, accountType, status, accountNumber);
    }

    @Transactional
    public void validateNoNegativeBalance(Account account, BigDecimal amount) {
        if (account.getAccountType() == AccountType.AHORROS) {
            if (account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Savings accounts cannot have negative balance", HttpStatus.CONFLICT);
            }
        }
        // For checking accounts, overdraft policy depends on the bank rules
    }

    private String generateUniqueAccountNumber(AccountType type) {
        // Savings start with 53; Checking with 33
        String prefix = (type == AccountType.CORRIENTE) ? "53" : "33";
        String accountNumber;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            // Generate 8 random digits to complete 10-digit account number
            int number = ThreadLocalRandom.current().nextInt(10000000, 100000000); // 8 digits
            accountNumber = prefix + number;
            attempts++;

            if (attempts > MAX_ATTEMPTS) {
                throw new BusinessException("Could not generate a unique account number", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
