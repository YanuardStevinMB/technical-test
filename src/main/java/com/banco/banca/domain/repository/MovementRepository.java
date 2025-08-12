package com.banco.banca.domain.repository;

import com.banco.banca.domain.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<Movement, UUID> {

    List<Movement> findTop10ByAccount_IdOrderByDateDesc(UUID accountId);
}
