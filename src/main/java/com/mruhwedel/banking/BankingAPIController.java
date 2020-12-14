package com.mruhwedel.banking;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

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

    @Parameter(name = "iban", schema = @Schema(type = "string"))
    @PostMapping("{iban}/deposit")
    void deposit(
            @PathVariable("iban") Iban iban,
            @RequestBody Money money
    ) {
        accountService.deposit(iban, money);
    }

    @Parameters({
            @Parameter(name = "from", schema = @Schema(type = "string")),
            @Parameter(name = "to", schema = @Schema(type = "string"))
    }
    )


    @PostMapping("{from}/transfer-to/{to}")
    void transfer(
            @Valid @PathVariable Iban from,
            @Valid @PathVariable Iban to,
            @Valid @RequestBody Money money
    ) {
        if (!accountService.transfer(from, to, money)) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
    }

    @Parameter(name = "iban", schema = @Schema(type = "string"))
    @GetMapping("{iban}")
    Money balance(@Valid @PathVariable("iban") Iban iban) {
        return accountService.getBalance(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Parameter(name = "iban", schema = @Schema(type = "string"))
    @GetMapping("{iban}/transactions")
    List<TransactionLog> transactions(@Valid @PathVariable("iban") Iban iban) {
        return accountService.getTransactions(iban);
    }

    @GetMapping
    List<Account> getAllFiltered(@RequestParam(value = "accountTypes", required = false)
                                         List<AccountType> accountTypes) {
        return accountService.getAllFiltered(accountTypes);
    }

    @Parameter(name = "iban", schema = @Schema(type = "string"))
    @PutMapping("{iban}/lock")
    void lock(@PathVariable("iban") Iban iban) {
        accountService
                .lock(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Parameter(name = "iban", schema = @Schema(type = "string"))
    @DeleteMapping("{iban}/lock")
    void unlock(@PathVariable("iban") Iban iban) {
        accountService
                .unlock(iban)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
