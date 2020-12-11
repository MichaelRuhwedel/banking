package com.mruhwedel.banking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountService {

    void deposit(IBAN selected, Amount amount) {
        log.info("{}+= {}", selected, amount);
    }

    void transfer(IBAN from, IBAN to, Amount amount) {
        log.info("{}->{} {} ", from, to, amount);
    }

    void getBalance(IBAN account, AccountType accountType) {
        log.info("{}[{}]", account, accountType);
    }

    void getAll(AccountType accountType) {
        log.info("{}*", accountType);
    }
}
