package com.mruhwedel.banking

import com.mruhwedel.banking.domain.AccountType
import com.mruhwedel.banking.domain.Iban
import com.mruhwedel.banking.domain.Money

import static com.mruhwedel.banking.domain.AccountType.CHECKING

class BankingTestData {
    static final Iban IBAN = new Iban('DE75512108001245126101') // from https://www.iban.com/structure
    static final Iban IBAN_CHECKING = IBAN // from https://www.iban.com/structure
    static final Iban IBAN_SAVINGS = new Iban('DE75512108001245126102') // from https://www.iban.com/structure
    static final Iban IBAN_LOAN = new Iban('DE75512108001245126103') // from https://www.iban.com/structure

    static final Iban IBAN_2 = new Iban('DE75512108001245126100') // modified from -^
    static final Money MONEY = new Money(new BigDecimal('123.12'))
    static final AccountType ACCOUNT_TYPE = CHECKING
}
