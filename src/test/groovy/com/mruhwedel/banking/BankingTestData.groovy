package com.mruhwedel.banking

import static com.mruhwedel.banking.AccountType.CHECKING

class BankingTestData {

    public static IBAN = new Iban('DE75512108001245126199') // from https://www.iban.com/structure
    public static IBAN_2 = new Iban('DE75512108001245126100') // modified from -^
    public static MONEY = new Money(new BigDecimal(123))
    public static ACCOUNT_TYPE = CHECKING
}
