package com.mruhwedel.banking;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.math.BigDecimal;

import static com.mruhwedel.banking.AccountType.SAVINGS;
import static java.math.BigDecimal.ZERO;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "iban")
public class Account {
    public Account(Iban iban, Account checking) {
        this(SAVINGS, iban); // when we get an account reference, our instance is a savings account
        this.checking = checking;
    }

    public Account(AccountType accountType, Iban iban) {
        this.accountType = accountType;
        this.iban = iban.getValue();
    }


    private AccountType accountType;
    private BigDecimal balance = ZERO;
    private boolean locked;

    @Id
    private String iban;

    public Iban getIban() {
        return new Iban(iban);
    }

    @OneToOne
    private Account checking;

    void deposit(Money money) {
        balance = balance.add(money.getAmount());
    }

    public void withdraw(Money amount) {
        balance = balance.subtract(amount.getAmount());
    }

    public Money getBalance() {
        return new Money(balance);
    }
}
