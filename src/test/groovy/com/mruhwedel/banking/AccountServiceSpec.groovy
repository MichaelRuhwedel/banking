package com.mruhwedel.banking

import spock.lang.Specification

import static com.mruhwedel.banking.BankingTestData.ACCOUNT_TYPE
import static com.mruhwedel.banking.BankingTestData.IBAN_2
import static com.mruhwedel.banking.BankingTestData.MONEY
import static com.mruhwedel.banking.BankingTestData.IBAN
import static java.math.BigDecimal.ZERO

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
        1 * service.accountRepository.findByIban(IBAN.value) >> Optional.of(account)
        1 * service.accountRepository.save({ Account it ->
            it.balance == MONEY
        })
    }

    def "transfer() deducts the money from a and deposits it in b"() {
        given: 'accounts'
        def a = new Account(ACCOUNT_TYPE, IBAN) // account starts with 0.0 balance
        a.deposit(new Money(123456.0))
        def b = new Account(ACCOUNT_TYPE, IBAN_2) // account starts with 0.0 balance

        and: 'cash'
        def originalBalance = a.balance.amount
        def transfer = new Money(234567.0)

        when:
        service.transfer(IBAN, IBAN_2, transfer)

        then: 'load the accounts'
        1 * service.accountRepository.findByIban(IBAN.value) >> Optional.of(a)
        1 * service.accountRepository.findByIban(IBAN_2.value) >> Optional.of(b)

        and: 'persist balance a'
        1 * service.accountRepository.save({ Account it ->
            it.iban == IBAN
            it.balance.amount == originalBalance - transfer.amount
        })

        and: 'persist balance b'
        1 * service.accountRepository.save({
            it.iban == IBAN_2
            it.balance == transfer
        })

        and: 'creates a transaction log entry'
        1 * service.transactionRepository.save({ TransactionLog it ->
            it.ibanFrom == IBAN
            it.ibanTo == IBAN_2
            it.amount == transfer.amount
        })

    }

    def "create() Creates an account with the given type "() {
        when:
        service.create(new AccountCreationDto(ACCOUNT_TYPE, null))

        then:
        1 * service.accountRepository.save({ Account a ->
            a.accountType == ACCOUNT_TYPE
            a.balance.amount == 0.0
            a.iban =~ /DE[0-9]{20}/
        }) >> Stub(Account)
    }

    def "create() Savings account references checking "() {
        given:
        def accountType = AccountType.SAVINGS
        def checkingAccount = Stub(Account)

        when:
        service.create(new AccountCreationDto(accountType, IBAN))

        then:
        1 * service.accountRepository.getOne(IBAN.value) >> checkingAccount
        1 * service.accountRepository.save({ Account a ->
            a.accountType == accountType
            a.balance == new Money(ZERO)
            a.checking == checkingAccount
        }) >>  Stub(Account)
    }

    def "getBalance(): Should return the balance"() {
        given:
        def account = new Account()
        account.deposit(new Money(123.0))

        when:
        def result = service.getBalance(IBAN)

        then:
        service.accountRepository.findByIban(IBAN.value) >> Optional.of(account)

        result
                .map(account.balance::equals)
                .orElse(false)
    }

    def 'getAll() will filter by account type'() {
        given:
        def filter = [ACCOUNT_TYPE]
        def accounts = [new Account()]

        when:
        def result = service.getAllFiltered(filter)

        then:
        1 * service.accountRepository.findByAccountType(filter) >> accounts
        result == accounts
    }
}
