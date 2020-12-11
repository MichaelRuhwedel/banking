package com.mruhwedel.banking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/api/account")
public class BankingAPIController {

    @PostMapping("{selected}/deposit")
    void deposit(IBAN selected, Amount amount) {
        log.info("{}+= {}", selected, amount);
    }

    @PostMapping("{from}/transfer/${to}")
    void transfer(IBAN from, IBAN to, Amount amount) {
        log.info("{}->{} {} ", from, to, amount);
    }

    @GetMapping("{selected}")
    void balance(IBAN account, AccountType accountType) {
        log.info("{}[{}]", account, accountType);
    }

    @GetMapping()
    void getAll(AccountType accountType) {
        log.info("{}*", accountType);
    }
}
