package com.mruhwedel.banking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static java.util.UUID.randomUUID;

@Data
@Entity
@NoArgsConstructor
public class Account {

    public Account(AccountType accountType, Iban iban) {
        this.id = randomUUID();
        this.accountType = accountType;
        this.iban = iban;
    }

    @Id
    private UUID id;
    private AccountType accountType;
    private BigDecimal balance = ZERO;
    private Iban iban;

    void deposit(Money money) {
        balance = balance.add(money.getAmount());
    }

}
