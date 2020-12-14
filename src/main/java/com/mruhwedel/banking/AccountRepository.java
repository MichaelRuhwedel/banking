package com.mruhwedel.banking;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    @Query("SELECT a FROM Account a WHERE a.iban = ?1")
    Optional<Account> findByIban(Iban iban);

    @Query("SELECT a FROM Account a WHERE a.accountType = ?1")
    List<Account> findByAccountType(AccountType accountType);
}
