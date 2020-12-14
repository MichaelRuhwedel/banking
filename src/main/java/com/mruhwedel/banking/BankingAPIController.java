package com.mruhwedel.banking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController("/api/account")
public class BankingAPIController {

    private final AccountService accountService;

    @PostMapping("{selected}/deposit")
    void deposit(
            @RequestParam Iban ibanOfAccount,
            @RequestBody Money money
    ) {
        accountService.deposit(ibanOfAccount, money);
    }

    @PostMapping("{from}/transfer-to/${to}")
    void transfer(
            @RequestParam Iban from,
            @RequestParam Iban to,
            @RequestBody Money money
    ) {
        accountService.transfer(from, to, money);
    }

    @GetMapping("/{selected}")
    void balance(@RequestParam Iban selected) {
        accountService.getBalance(selected);
    }

    @GetMapping
    List<Account> getAllFiltered(@RequestParam List<AccountType> filter) { return accountService.getAllFiltered(filter);
    }
}
