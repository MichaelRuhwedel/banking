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

import static com.mruhwedel.banking.TransferResult.TRANSFERRED;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class BankingAPIController {

    private final AccountService accountService;

    @PostMapping("/api/accounts")
    @ResponseStatus(CREATED)
    Iban createAccount(@RequestBody AccountCreationDto accountCreationDto) {
        return accountService.create(accountCreationDto);
    }

    @PostMapping("/api/accounts/{selected}/deposit")
    void deposit(
            @PathVariable("selected") Iban selected,
            @RequestBody Money money
    ) {
        accountService.deposit(selected, money);
    }

    @PostMapping("/api/accounts/{from}/transfer-to/{to}")
    void transfer(
            @PathVariable Iban from,
            @PathVariable Iban to,
            @RequestBody Money money
    ) {
        if (accountService.transfer(from, to, money) != TRANSFERRED) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @GetMapping("/api/accounts/{selected}")
    Optional<Money> balance(@Valid @PathVariable("selected") Iban selected) {
        return accountService.getBalance(selected);
    }

    @GetMapping("/api/accounts/{selected}/transactions")
    List<TransactionLog> transactions(@PathVariable("selected") Iban selected) {
        return accountService.getTransactions(selected);
    }

    @GetMapping("/api/accounts")
    List<Account> getAllFiltered(@RequestParam("accountTypes") List<AccountType> accountTypes) {
        return accountService.getAllFiltered(accountTypes);
    }
}
