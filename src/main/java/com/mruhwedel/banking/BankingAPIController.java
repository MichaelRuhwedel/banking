package com.mruhwedel.banking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

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
    Optional<Money> balance(@Valid @PathVariable("iban") Iban iban) {
        return accountService.getBalance(iban);
    }

    @GetMapping("{iban}/transactions")
    List<TransactionLog> transactions(@PathVariable("iban") Iban iban) {
        return accountService.getTransactions(iban);
    }

    @GetMapping
    List<Account> getAllFiltered(@RequestParam("accountTypes") List<AccountType> accountTypes) {
        return accountService.getAllFiltered(accountTypes);
    }

    @PutMapping("{iban}/lock")
    void lock(@PathVariable("iban") Iban iban) {
        accountService.lock(iban);
    }

    @DeleteMapping("{iban}/lock")
    void unlock(@PathVariable("iban") Iban iban) {
        accountService.lock(iban);
    }
}
