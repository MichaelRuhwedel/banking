package com.mruhwedel.banking.repositories;


import com.mruhwedel.banking.domain.Account;
import com.mruhwedel.banking.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("SELECT a " +
            "FROM Account a " +
            "WHERE a.accountType in ?1")
    List<Account> findByAccountType(List<AccountType> accountType);
}
