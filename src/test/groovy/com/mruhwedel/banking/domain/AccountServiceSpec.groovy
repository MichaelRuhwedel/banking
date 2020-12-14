package com.mruhwedel.banking.domain

import com.mruhwedel.banking.domain.*
import com.mruhwedel.banking.repositories.AccountRepository
import com.mruhwedel.banking.repositories.TransactionRepository
import spock.lang.Specification
import spock.lang.Unroll

import static com.mruhwedel.banking.BankingTestData.*
import static com.mruhwedel.banking.domain.AccountType.CHECKING
import static com.mruhwedel.banking.domain.AccountType.PRIVATE_LOAN
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
        1 * service.accountRepository.findById(IBAN.value) >> Optional.of(account)
        1 * service.accountRepository.save({ Account it ->
            it.balance == MONEY.amount
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
        1 * service.accountRepository.findById(IBAN.value) >> Optional.of(a)
        1 * service.accountRepository.findById(IBAN_2.value) >> Optional.of(b)

        and: 'persist balance a'
        1 * service.accountRepository.save({ Account it ->
            it.iban == IBAN.value
            it.balance == originalBalance - transfer.amount
        })

        and: 'persist balance b'
        1 * service.accountRepository.save({
            it.iban == IBAN_2.value
            it.balance == transfer.amount
        })

        and: 'creates a transaction log entry'
        1 * service.transactionRepository.save({ TransactionLog it ->
            it.ibanFrom == IBAN.value
            it.ibanTo == IBAN_2.value
            it.amount == transfer.amount
        })

    }

    @Unroll
    def 'transfer() Checking to #destination is possible'(AccountType destination) {
        given: 'accounts'
        def sourceAccount = new Account(CHECKING, IBAN) // account starts with 0.0 balance
        sourceAccount.deposit(MONEY)
        service.accountRepository.findById(sourceAccount.iban) >> Optional.of(sourceAccount)

        def destinationAccount = new Account(destination, IBAN_2)
        service.accountRepository.findById(destinationAccount.iban) >> Optional.of(destinationAccount)

        when:
        def result = service.transfer(new Iban(sourceAccount.iban), new Iban(destinationAccount.iban), MONEY)

        then:
        result

        where:
        destination << AccountType.values()
    }


    def 'transfer() IMPOSSIBLE from a locked account'() {
        given: 'accounts'
        def sourceAccount = new Account(CHECKING, IBAN) // account starts with 0.0 balance
        sourceAccount.setLocked(true)

        service.accountRepository.findById(sourceAccount.iban) >> Optional.of(sourceAccount)
        service.accountRepository.findById(IBAN_2.value) >> Optional.of(Stub(Account))

        when:
        def result = service.transfer(new Iban(sourceAccount.iban), IBAN_2, MONEY)

        then:
        !result
    }

    def "transfer() from SAVINGS to referenced CHECKING is possible"() {
        given: 'a checking account, ...'
        def accountChecking = new Account(CHECKING, IBAN_CHECKING) // account starts with 0.0 balance
        accountChecking.deposit(MONEY)
        service.accountRepository.findById(accountChecking.iban) >> Optional.of(accountChecking)

        and: '... a savings account that belongs to it.'
        def accountSavings = new Account(IBAN_SAVINGS, accountChecking)
        service.accountRepository.findById(accountSavings.iban) >> Optional.of(accountSavings)

        when:
        def result = service.transfer(new Iban(accountChecking.iban), new Iban(accountSavings.iban), MONEY)

        then:
        result
    }

    def "transfer() from SAVINGS to any NON referenced account is NOT possible"() {
        given: 'a savings account of some account'
        def accountSavings = new Account(IBAN_SAVINGS, Stub(Account)) // is associated with some account
        service.accountRepository.findById(accountSavings.iban) >> Optional.of(accountSavings)

        and: 'a checking account'
        def accountChecking = new Account(CHECKING, IBAN_CHECKING)
        service.accountRepository.findById(IBAN_CHECKING.value) >> Optional.of(accountChecking)

        when:
        def result = service.transfer(new Iban(accountSavings.iban), IBAN_CHECKING, MONEY)

        then:
        !result

        and:
        0 * service.accountRepository.save(_)
        0 * service.transactionRepository.save(_)
    }

    /**
     * Private loan account - transferring money from any account is possible.
     * Withdrawal is not possible
     *
     * tbh i don't really understand the spec here, please tell me what you'd expect to happen.
     * My interpretation is below:
     */
    @Unroll
    def "transfer() from PRIVATE_LOAN to any Account is NOT possible"() {
        given: 'a private loan, ...'
        def accountPrivateLoan = new Account(PRIVATE_LOAN, IBAN_LOAN) // account starts with 0.0 balance
        accountPrivateLoan.deposit(MONEY)
        service.accountRepository.findById(accountPrivateLoan.iban) >> Optional.of(accountPrivateLoan)

        and: '... any other account.'
        def someAccount = new Account(IBAN, accountPrivateLoan)
        service.accountRepository.findById(someAccount.iban) >> Optional.of(someAccount)

        when:
        def result = service.transfer(new Iban(accountPrivateLoan.iban), new Iban(someAccount.iban), MONEY)

        then:
        !result

        where:
        destinationAccount << AccountType.values()
    }

    def "create() Creates an account with the given type "() {
        when:
        service.create(ACCOUNT_TYPE, null)

        then:
        1 * service.accountRepository.save({ Account a ->
            a.accountType == ACCOUNT_TYPE
            a.balance == 0.0
            a.iban =~ /DE[0-9]{20}/
        }) >> Stub(Account)
    }

    def "create() Savings account references checking "() {
        given:
        def accountType = AccountType.SAVINGS
        def checkingAccount = Stub(Account)

        when:
        service.create(accountType, IBAN)

        then:
        1 * service.accountRepository.getOne(IBAN.value) >> checkingAccount
        1 * service.accountRepository.save({ Account a ->
            a.accountType == accountType
            a.balance == ZERO
            a.checking == checkingAccount
        }) >> Stub(Account)
    }

    def "getBalance(): Should return the balance"() {
        given:
        def account = new Account()
        account.deposit(new Money(123.0))

        when:
        def result = service.getBalance(IBAN)

        then:
        service.accountRepository.findById(IBAN.value) >> Optional.of(account)

        result
                .map(m -> account.balance == m.amount)
                .orElse(false)
    }

    def 'getAll() will filter by account type'() {
        given:
        def filter = [ACCOUNT_TYPE]
        def accounts = [Stub(Account)]

        when:
        def result = service.getAllFiltered(filter)

        then:
        1 * service.accountRepository.findByAccountType(filter) >> accounts
        result == accounts
    }

    def 'lock() will lock an account'() {
        when:
        service.lock(IBAN)

        then:
        1 * service.accountRepository.findById(IBAN.value) >> Optional.of(new Account())
        1 * service.accountRepository.save({ Account it -> it.locked })
    }

    def 'unlock() will unlock an account'() {
        when:
        service.unlock(IBAN)

        then:
        1 * service.accountRepository.findById(IBAN.value) >> Optional.of(new Account(locked: true))
        1 * service.accountRepository.save({ Account it -> !it.locked })
    }
}
