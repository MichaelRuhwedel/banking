package com.mruhwedel.banking;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;

@Data
@Entity
@NoArgsConstructor

public class Account {

    public Account(AccountType accountType, Iban iban) {
        this.accountType = accountType;
        this.iban = iban.getValue();
    }


    private AccountType accountType;
    private BigDecimal balance = ZERO;

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
