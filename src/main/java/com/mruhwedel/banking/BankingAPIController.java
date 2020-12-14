package com.mruhwedel.banking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class BankingAPIController {

    private final AccountService accountService;

    @PostMapping("/api/accounts")
    @ResponseStatus(CREATED)
    void createAccount(@RequestBody AccountCreationDto accountCreationDto) {
        accountService.create( accountCreationDto);
    }

    @PostMapping("/api/accounts/{selected}/deposit")
    void deposit(
            @RequestParam Iban ibanOfAccount,
            @RequestBody Money money
    ) {
        accountService.deposit(ibanOfAccount, money);
    }

    @PostMapping("/api/accounts/{from}/transfer-to/{to}")
    void transfer(
            @RequestParam Iban from,
            @RequestParam Iban to,
            @RequestBody Money money
    ) {
        accountService.transfer(from, to, money);
    }

    @GetMapping("/api/accounts/{selected}")
    void balance(@RequestParam Iban selected) {
        accountService.getBalance(selected);
    }

    @GetMapping("/api/accounts/{selected}/transactions")
    void transactions(@RequestParam Iban selected) {
        accountService.getTransactions(selected);
    }

    @GetMapping("/api/accounts")
    List<Account> getAllFiltered(@RequestParam List<AccountType> accountTypes) { return accountService.getAllFiltered(accountTypes);
    }
}
