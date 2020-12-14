package com.mruhwedel.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionLog, UUID>{

    @Query(" SELECT t " +
            "FROM TransactionLog t " +
            "WHERE " +
            "   t.ibanFrom = ?1 OR " +
            "   t.ibanTo = ?1")
    List<TransactionLog> findByIban(Iban iban);
}
