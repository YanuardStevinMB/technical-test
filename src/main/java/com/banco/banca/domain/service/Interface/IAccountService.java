package com.banco.banca.domain.service.Interface;

import com.banco.banca.domain.entity.Account;
import com.banco.banca.domain.entity.AccountStatus;
import com.banco.banca.domain.entity.AccountType;
import com.banco.banca.domain.entity.Movement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IAccountService {

        Account create(UUID clientId, AccountType accountType, Boolean gmfExempt, String ownerUser);

        Account createWithBalance(UUID clientId, AccountType accountType, BigDecimal initialBalance,
                                  Boolean gmfExempt, String ownerUser);

        Account get(UUID id);

        Account updateStatus(UUID id, AccountStatus newStatus);

        List<Movement> lastMovements(UUID accountId, int limit);

        List<Account> search(UUID clientId, AccountType accountType, AccountStatus status, String accountNumber);

        void validateNoNegativeBalance(Account account, BigDecimal amount);


}
