package com.mruhwedel.banking;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static com.mruhwedel.banking.AccountType.SAVINGS;
import static com.mruhwedel.banking.TransferResult.*;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@Transactional
@Getter(PACKAGE) // visible for testing
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    Iban create(AccountCreationDto creationDto) {
        log.info("* {} ref: {}", creationDto.getAccountType(), creationDto.getReferenceIban());
        AccountType accountType = creationDto.getAccountType();

        Account entity = new Account(accountType, generateRandomIban());

        if (accountType == SAVINGS) {
            Account checking = accountRepository.getOne(creationDto.getReferenceIban().getValue());
            entity.setChecking(checking);
        }
        return accountRepository
                .save(entity)
                .getIban();
    }

    private Iban generateRandomIban() {
        return new Iban("DE" + RandomStringUtils.randomNumeric(20));
    }

    void deposit(Iban selected, Money money) {
        log.info("{}+= {}", selected.getValue(), money.getAmount());

        getByIban(selected).ifPresent(account -> {
            account.deposit(money);
            accountRepository.save(account);
        });
    }

    TransferResult transfer(
            @NonNull Iban from,
            @NonNull Iban to,
            @NonNull Money amount) {
        log.info("{}->{} {} ", from.getValue(), to.getValue(), amount);
        return Optionals
                .withBoth(getByIban(from), getByIban(to))
                .map(
                        p -> {
                            Account source = p.getFirst();
                            Account destination = p.getSecond();

                            if(source.getAccountType() == SAVINGS && !source.getChecking().equals(destination)){
                                return INVALID_ACCOUNT_TARGET;
                            }

                            source.withdraw(amount);
                            destination.deposit(amount);

                            accountRepository.save(source);
                            accountRepository.save(destination);

                            transactionRepository.save(new TransactionLog(source, destination, amount));

                            return TRANSFERRED;
                        }
                )
                .orElse(ACCOUNT_NONEXISTENT);
    }

    private Optional<Account> getByIban(Iban from) {
        return accountRepository.findByIban(from.getValue());
    }

    Optional<Money> getBalance(Iban account) {
        log.info("{}?", account.getValue());
        return getByIban(account)
                .map(Account::getBalance);
    }

    List<Account> getAllFiltered(List<AccountType> filter) {
        log.info("{}*", filter);
        return accountRepository.findByAccountType(filter);
    }

    public List<TransactionLog> getTransactions(Iban selected) {
        log.info("{}t", selected.getValue());
        return transactionRepository.findByIban(selected);
    }
}
