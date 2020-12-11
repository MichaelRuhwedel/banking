package com.mruhwedel.banking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountService {

    void deposit(Iban selected, Money amount) {
        log.info("{}+= {}", selected, amount);
    }

    void transfer(Iban from, Iban to, Money amount) {
        log.info("{}->{} {} ", from, to, amount);
    }

    void getBalance(Iban account) {
        log.info("{}[{}]", account);
    }

    void getAll(AccountType accountType) {
        log.info("{}*", accountType);
    }
}
