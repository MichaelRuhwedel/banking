package com.mruhwedel.banking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController("/api/account")
public class BankingAPIController {

    private final AccountService accountService;

    @PostMapping("{selected}/deposit")
    void deposit(@RequestParam
                         IBAN selected,
                 @RequestBody Amount amount) {
        accountService.deposit(selected, amount);
    }

    @PostMapping("{from}/transfer-to/${to}")
    void transfer(
            @RequestParam IBAN from,
            @RequestParam IBAN to,
            @RequestBody Amount amount
    ) {
        accountService.transfer(from, to, amount);
    }

    @GetMapping("{selected}")
    void balance(
            @RequestParam IBAN account,
            @RequestBody AccountType accountType
    ) {
        accountService.getBalance(account, accountType);
    }

    @GetMapping
    void getAll(@RequestParam AccountType accountType) {
        accountService.getAll(accountType);
    }
}
