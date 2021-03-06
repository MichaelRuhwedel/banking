package com.mruhwedel.banking.transactionlog;

import com.mruhwedel.banking.account.Account;
import com.mruhwedel.banking.account.Money;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
public class TransactionLog {

    @Id
    private UUID id;

    private String ibanFrom;
    private String ibanTo;
    private BigDecimal amount;

    public TransactionLog(
            @NonNull Account from,
            @NonNull Account to,
            @NonNull Money money
    ) {
        this.ibanFrom = from.getIban();
        this.ibanTo = to.getIban();
        this.amount = money.getAmount();

        id = UUID.randomUUID();
    }

}
