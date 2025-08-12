package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Account;
import com.banco.banca.domain.entity.AccountStatus;
import com.banco.banca.domain.entity.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByClient_Id(UUID clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockingById(UUID id);

    // Search with optional filters
    @Query("""
            SELECT a FROM Account a
            WHERE (:clientId IS NULL OR a.client.id = :clientId)
              AND (:accountType IS NULL OR a.accountType = :accountType)
              AND (:status IS NULL OR a.status = :status)
              AND (:accountNumber IS NULL OR a.accountNumber = :accountNumber)
            ORDER BY a.createdAt DESC
            """)
    List<Account> search(
            @Param("clientId") UUID clientId,
            @Param("accountType") AccountType accountType,
            @Param("status") AccountStatus status,
            @Param("accountNumber") String accountNumber
    );
}
