package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdentificationNumber(String identificationNumber);

    boolean existsByIdentificationNumber(String identificationNumber);

    // Composite uniqueness validation
    boolean existsByIdentificationTypeAndIdentificationNumber(String identificationType, String identificationNumber);

    // Check if client has associated accounts
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.client.id = :clientId")
    boolean hasAssociatedAccounts(@Param("clientId") UUID clientId);

    // Search with optional filters (PostgreSQL) using ILIKE on VARCHAR
    @Query(value = """
            SELECT c.*
            FROM clients c
            WHERE (:identificationType IS NULL OR c.identification_type = :identificationType)
              AND (:identificationNumber IS NULL OR c.identification_number = :identificationNumber)
              AND (:firstName IS NULL OR c.first_name ILIKE CONCAT('%', :firstName, '%'))
              AND (:lastName IS NULL OR c.last_name ILIKE CONCAT('%', :lastName, '%'))
              AND (:email IS NULL OR c.email ILIKE CONCAT('%', :email, '%'))
            ORDER BY c.created_at DESC
            """, nativeQuery = true)
    List<Client> search(
            @Param("identificationType") String identificationType,
            @Param("identificationNumber") String identificationNumber,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email
    );
}
