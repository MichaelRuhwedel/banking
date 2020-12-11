package com.mruhwedel.banking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    private UUID id;
    private AccountType accountType;
    private BigDecimal balance = ZERO;

    void  deposit(Money money){
        balance = balance.add(money.getAmount());
    }

}
