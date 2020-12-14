package com.mruhwedel.banking

import static com.mruhwedel.banking.AccountType.CHECKING

class BankingTestData {

    public static Iban IBAN = new Iban('DE75512108001245126101') // from https://www.iban.com/structure
    public static Iban IBAN_CHECKING = new Iban('DE75512108001245126102') // from https://www.iban.com/structure
    public static Iban IBAN_LOAN = new Iban('DE75512108001245126103') // from https://www.iban.com/structure

    public static Iban IBAN_2 = new Iban('DE75512108001245126100') // modified from -^
    public static Money MONEY = new Money(new BigDecimal('123.12'))
    public static AccountType ACCOUNT_TYPE = CHECKING

}
