package com.mruhwedel.banking

import spock.lang.Specification

import static com.mruhwedel.banking.BankingTestData.ACCOUNT_TYPE
import static com.mruhwedel.banking.BankingTestData.IBAN_2
import static com.mruhwedel.banking.BankingTestData.MONEY
import static com.mruhwedel.banking.BankingTestData.IBAN

class AccountServiceSpec extends Specification {
    def service = new AccountService(
            Mock(AccountRepository),
            Mock(TransactionRepository)
    )

    def 'deposit() should load an account, add the amount and persist it'() {
        given:
        def account = new Account() // account starts with 0.0 balance

        when:
        service.deposit(IBAN, MONEY)

        then:
        1 * service.accountRepository.findByIban(IBAN) >> Optional.of(account)
        1 * service.accountRepository.save({
            (it as Account).balance == MONEY.amount
        })
    }

    def "transfer() deducts the money from a and deposits it in b"() {
        given: 'accounts'
        def a = new Account(ACCOUNT_TYPE, IBAN) // account starts with 0.0 balance
        a.deposit(new Money(123456.0))
        def b = new Account(ACCOUNT_TYPE, IBAN_2) // account starts with 0.0 balance

        and: 'cash'
        def originalBalance = a.balance
        def transfer = new Money(234567.0)

        when:
        service.transfer(IBAN, IBAN_2, transfer)

        then: 'load the accounts'
        1 * service.accountRepository.findByIban(IBAN) >> Optional.of(a)
        1 * service.accountRepository.findByIban(IBAN_2) >> Optional.of(b)

        and: 'persist balance a'
        1 * service.accountRepository.save({
            (it as Account).iban == IBAN.value
            (it as Account).balance == originalBalance - transfer.amount
        })

        and: 'persist balance b'
        1 * service.accountRepository.save({
            (it as Account).iban == IBAN_2.value
            (it as Account).balance == transfer.amount
        })

    }

    def "getBalance(): Should return the balance"() {
        given:
        def account = new Account()
        account.deposit(new Money(123.0))

        when:
        def result = service.getBalance(IBAN)

        then:
        service.accountRepository.findByIban(IBAN) >> Optional.of(account)

        result
                .map { it.amount == account.balance }
                .orElse(false)
    }

    def 'getAll() will filter by account type'() {
        given:
        def filter = ACCOUNT_TYPE
        def accounts = [new Account()]

        when:
        def result = service.getAllFiltered(filter)

        then:
        1 * service.accountRepository.findByAccountType(filter) >> accounts
        result == accounts

    }
}
