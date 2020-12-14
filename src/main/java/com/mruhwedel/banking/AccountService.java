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

import static com.mruhwedel.banking.AccountType.PRIVATE_LOAN;
import static com.mruhwedel.banking.AccountType.SAVINGS;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@Transactional
@Getter(PACKAGE) // visible for testing
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    Iban create(AccountType accountType, Iban referenceIban) {
        log.info("* {} ref: {}", accountType, referenceIban);

        Account entity = new Account(accountType, generateRandomIban());

        if (accountType == SAVINGS) {
            Account checking = accountRepository.getOne(referenceIban.getValue());
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

    boolean transfer(
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

                            if (isNotAllowed(source, destination)) {
                                return false;
                            }

                            source.withdraw(amount);
                            destination.deposit(amount);

                            accountRepository.save(source);
                            accountRepository.save(destination);

                            transactionRepository.save(new TransactionLog(source, destination, amount));

                            return true;
                        }
                )
                .orElse(FALSE);
    }

    private boolean isNotAllowed(Account source, Account destination) {
        AccountType sourceType = source.getAccountType();
        return source.isLocked() || destination.isLocked()
                || (sourceType == SAVINGS && !source.getChecking().equals(destination))
                || (sourceType == PRIVATE_LOAN);
    }

    private Optional<Account> getByIban(Iban from) {
        return accountRepository.findById(from.getValue());
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

    public void lock(Iban selected) {
        log.info("Locking {}", selected.getValue());
        Account account = accountRepository.getOne(selected.getValue());
        account.setLocked(true);
        accountRepository.save(account);
    }

    public void unlock(Iban selected) {
        log.info("UNLocking {}", selected.getValue());
        Account account = accountRepository.getOne(selected.getValue());
        account.setLocked(false);
    }

}
