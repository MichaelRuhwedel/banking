package com.mruhwedel.banking

import spock.lang.Specification

import static com.mruhwedel.banking.BankingTestData.ACCOUNT_TYPE
import static com.mruhwedel.banking.BankingTestData.MONEY
import static com.mruhwedel.banking.BankingTestData.IBAN

class AccountServiceSpec extends Specification {
    def service = new AccountService(
            Mock(AccountRepository)
    )

    def 'deposit will load an account, add the amount and persist it'() {
        given:
        def account = new Account(null, ACCOUNT_TYPE, 0.0)

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

    def "test getBalance"() {
//        given:
//        when:
//        then:
    }

    def "test getAll"() {
//        given:
//        when:
//        then:
    }
}
