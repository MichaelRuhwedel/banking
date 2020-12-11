package com.mruhwedel.banking

import spock.lang.Specification

import static com.mruhwedel.banking.BankingTestData.ACCOUNT_TYPE
import static com.mruhwedel.banking.BankingTestData.MONEY
import static com.mruhwedel.banking.BankingTestData.IBAN

class AccountServiceSpec extends Specification {
    def service = new AccountService(
            Mock(AccountRepository)
    )

    def 'deposit() should load an account, add the amount and persist it'() {
        given:
        def account = new Account( ACCOUNT_TYPE) // account starts with 0.0 balance

        when:
        service.deposit(IBAN, MONEY)

        then:
        1 * service.accountRepository.findByIban(IBAN) >> Optional.of(account)
        1 * service.accountRepository.save({
            (it as Account).balance == MONEY.amount
        })
    }

    def "test transfer"() {
//        given:
//
//        when:
//        then:
    }

    def "getBalance(): Should return the balance"() {
        given:
        def account = new Account(ACCOUNT_TYPE)
        account.deposit(new Money(123.0))

        when:
        def result = service.getBalance(IBAN)

        then:
        service.accountRepository.findByIban(IBAN) >> Optional.of(account)

        result
                .map {it.amount == account.balance}
                .orElse(false)
    }

    def "test getAll"() {
//        given:
//        when:
//        then:
    }
}
