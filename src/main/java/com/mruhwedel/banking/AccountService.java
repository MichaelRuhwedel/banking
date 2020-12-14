package com.mruhwedel.banking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.mruhwedel.banking.TransferResult.ACCOUNT_NONEXISTANT;
import static com.mruhwedel.banking.TransferResult.TRANSFERRED;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Component
@Getter(PACKAGE) // visible for testing
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    void deposit(Iban selected, Money money) {
        log.info("{}+= {}", selected, money);

        getByIban(selected).ifPresent(account -> {
            account.deposit(money);
            accountRepository.save(account);
        });
    }

    TransferResult transfer(Iban from, Iban to, Money amount) {
        log.info("{}->{} {} ", from, to, amount);
        return Optionals
                .withBoth(getByIban(from), getByIban(to))
                .map(
                        p -> {
                            Account source = p.getFirst();
                            Account destination = p.getSecond();

                            source.withdraw(amount);
                            destination.deposit(amount);

                            accountRepository.save(source);
                            accountRepository.save(destination);

                            transactionRepository.save(new TransactionLog(source, destination, amount));

                            return TRANSFERRED;
                        }
                )
                .orElse(ACCOUNT_NONEXISTANT);
    }

    private Optional<Account> getByIban(Iban from) {
        return accountRepository.findByIban(from);
    }

    Optional<Money> getBalance(Iban account) {
        log.info("{}", account);
        return getByIban(account)
                .map(Account::getBalance)
                .map(Money::new);
    }

    List<Account> getAllFiltered(List<AccountType> filter) {
        log.info("{}*", filter);
        return accountRepository.findByAccountType(filter);
    }

    public void getTransactions(Iban selected) {
        return transactionRepository.ge
    }
}
