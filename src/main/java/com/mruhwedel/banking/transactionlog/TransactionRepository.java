package com.mruhwedel.banking.transactionlog;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends CrudRepository<TransactionLog, UUID> {

    @Query(" SELECT t " +
            "FROM TransactionLog t " +
            "WHERE " +
            "   t.ibanFrom = ?1 OR " +
            "   t.ibanTo = ?1")
    List<TransactionLog> findByIbanFromOrIbanTo(String iban);
}
