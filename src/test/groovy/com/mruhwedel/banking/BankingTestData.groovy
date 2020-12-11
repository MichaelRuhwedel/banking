package com.mruhwedel.banking

class BankingTestData {

    public static IBAN = new IBAN('DE75512108001245126199') // from https://www.iban.com/structure
    public static IBAN_2 = new IBAN('DE75512108001245126100') // modified from -^
    public static AMOUNT = new Amount(new BigDecimal(123))
}
