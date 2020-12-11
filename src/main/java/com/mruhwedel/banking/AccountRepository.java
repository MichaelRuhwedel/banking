package com.mruhwedel.banking;


import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends CrudRepository<Account, UUID> {

    Optional<Account> findByIban(Iban iban);

    List<Account> findByAccountType(AccountType accountType);
}
