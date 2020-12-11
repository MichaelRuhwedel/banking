package com.mruhwedel.banking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@Getter(PACKAGE) // visible for testing
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    void deposit(Iban selected, Money money) {
        log.info("{}+= {}", selected, money);

        accountRepository
                .findByIban(selected)
                .ifPresent(account -> {
                    account.deposit(money);
                    accountRepository.save(account);
                });
    }

    void transfer(Iban from, Iban to, Money amount) {
        log.info("{}->{} {} ", from, to, amount);
    }

    Optional<Money> getBalance(Iban account) {
        log.info("{}[{}]", account);
        return accountRepository
                .findByIban(account)
                .map(Account::getBalance)
                .map(Money::new);

    }

    void getAll(AccountType accountType) {
        log.info("{}*", accountType);
    }
}
