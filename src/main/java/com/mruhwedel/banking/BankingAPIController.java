package com.mruhwedel.banking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class BankingAPIController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(CREATED)
    Iban createAccount(@RequestBody AccountCreationDto accountCreationDto) {
        return accountService.create(
                accountCreationDto.getAccountType(),
                accountCreationDto.getReferenceIban()
        );
    }

    @PostMapping("{iban}/deposit")
    void deposit(
            @PathVariable("iban") Iban iban,
            @RequestBody Money money
    ) {
        accountService.deposit(iban, money);
    }

    @PostMapping("{from}/transfer-to/{to}")
    void transfer(
            @PathVariable Iban from,
            @PathVariable Iban to,
            @RequestBody Money money
    ) {
        if (!accountService.transfer(from, to, money)) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @GetMapping("{iban}")
    Money balance(@Valid @PathVariable("iban") Iban iban) {
        return accountService.getBalance(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @GetMapping("{iban}/transactions")
    List<TransactionLog> transactions(@PathVariable("iban") Iban iban) {
        return accountService.getTransactions(iban);
    }

    @GetMapping
    List<Account> getAllFiltered(@RequestParam(value = "accountTypes", required = false, defaultValue = "[]")
                                         List<AccountType> accountTypes) {
        return accountService.getAllFiltered(accountTypes);
    }

    @PutMapping("{iban}/lock")
    void lock(@PathVariable("iban") Iban iban) {
        accountService
                .lock(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @DeleteMapping("{iban}/lock")
    void unlock(@PathVariable("iban") Iban iban) {
        accountService
                .unlock(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
