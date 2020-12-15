package com.mruhwedel.banking.account;

import com.mruhwedel.banking.transactionlog.TransactionLog;
import com.mruhwedel.banking.transactionlog.TransactionRepository;
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

import static com.mruhwedel.banking.account.AccountType.PRIVATE_LOAN;
import static com.mruhwedel.banking.account.AccountType.SAVINGS;
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

    public Iban create(AccountType accountType, Iban referenceIban) {
        log.info("* {} ref: {}", accountType, referenceIban);

        Account newAccount = new Account(accountType, generateRandomIban());

        if (accountType == SAVINGS) {
            Account checking = accountRepository
                    .findById(referenceIban.getValue())
                    .orElseThrow(IllegalArgumentException::new);

            newAccount.setChecking(checking);
        }
        Account created = accountRepository.save(newAccount);

        return new Iban(created.getIban());
    }

    private Iban generateRandomIban() {
        return new Iban("DE" + RandomStringUtils.randomNumeric(20));
    }

    public void deposit(Iban iban, Money money) {
        log.info("{}+= {}", iban.getValue(), money.getAmount());

        getByIban(iban).ifPresent(account -> {
            account.deposit(money);
            accountRepository.save(account);
        });
    }

    public boolean transfer(
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

    public Optional<Money> getBalance(Iban account) {
        log.info("{}?", account.getValue());
        return getByIban(account)
                .map(Account::getBalance)
                .map(Money::new);
    }

    public List<Account> getAllFiltered(List<AccountType> filter) {
        log.info("{}*", filter);
        return accountRepository.findByAccountTypeIn(filter);
    }

    public List<TransactionLog> getTransactions(Iban iban) {
        log.info("{}t", iban.getValue());
        return transactionRepository.findByIbanFromOrIbanTo(iban.getValue());
    }

    public Optional<Account> lock(Iban iban) {
        log.info("Locking {}", iban.getValue());
        return toggleLock(iban, true);
    }

    public Optional<Account> unlock(Iban iban) {
        log.info("UNLocking {}", iban.getValue());
        return toggleLock(iban, false);
    }

    private Optional<Account> toggleLock(Iban iban, boolean locked) {
        Optional<Account> account = accountRepository.findById(iban.getValue());
        account.ifPresent(a -> {
            a.setLocked(locked);
            accountRepository.save(a);
        });
        return account;
    }
}
