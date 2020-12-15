package com.mruhwedel.banking.account


import com.mruhwedel.banking.transactionlog.TransactionLog
import com.mruhwedel.banking.transactionlog.TransactionRepository
import spock.lang.Specification
import spock.lang.Unroll

import static com.mruhwedel.banking.account.AccountType.CHECKING
import static com.mruhwedel.banking.account.AccountType.PRIVATE_LOAN
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
        service.deposit(AccountTestData.IBAN, AccountTestData.MONEY)

        then:
        1 * service.accountRepository.findById(AccountTestData.IBAN.value) >> Optional.of(account)
        1 * service.accountRepository.save({ Account it ->
            it.balance == AccountTestData.MONEY.amount
        })
    }

    def "transfer() deducts the money from a and deposits it in b"() {
        given: 'accounts'
        def a = new Account(AccountTestData.ACCOUNT_TYPE, AccountTestData.IBAN) // account starts with 0.0 balance
        a.deposit(new Money(123456.0))
        def b = new Account(AccountTestData.ACCOUNT_TYPE, AccountTestData.IBAN_2) // account starts with 0.0 balance

        and: 'cash'
        def originalBalance = a.balance
        def transfer = new Money(234567.0)

        when:
        service.transfer(AccountTestData.IBAN, AccountTestData.IBAN_2, transfer)

        then: 'load the accounts'
        1 * service.accountRepository.findById(AccountTestData.IBAN.value) >> Optional.of(a)
        1 * service.accountRepository.findById(AccountTestData.IBAN_2.value) >> Optional.of(b)

        and: 'persist balance a'
        1 * service.accountRepository.save({ Account it ->
            it.iban == AccountTestData.IBAN.value
            it.balance == originalBalance - transfer.amount
        })

        and: 'persist balance b'
        1 * service.accountRepository.save({
            it.iban == AccountTestData.IBAN_2.value
            it.balance == transfer.amount
        })

        and: 'creates a transaction log entry'
        1 * service.transactionRepository.save({ TransactionLog it ->
            it.ibanFrom == AccountTestData.IBAN.value
            it.ibanTo == AccountTestData.IBAN_2.value
            it.amount == transfer.amount
        })

    }

    @Unroll
    def 'transfer() Checking to #destination is possible'(AccountType destination) {
        given: 'accounts'
        def sourceAccount = new Account(CHECKING, AccountTestData.IBAN) // account starts with 0.0 balance
        sourceAccount.deposit(AccountTestData.MONEY)
        service.accountRepository.findById(sourceAccount.iban) >> Optional.of(sourceAccount)

        def destinationAccount = new Account(destination, AccountTestData.IBAN_2)
        service.accountRepository.findById(destinationAccount.iban) >> Optional.of(destinationAccount)

        when:
        def result = service.transfer(new Iban(sourceAccount.iban), new Iban(destinationAccount.iban), AccountTestData.MONEY)

        then:
        result

        where:
        destination << AccountType.values()
    }


    def 'transfer() IMPOSSIBLE from a locked account'() {
        given: 'accounts'
        def sourceAccount = new Account(CHECKING, AccountTestData.IBAN) // account starts with 0.0 balance
        sourceAccount.setLocked(true)

        service.accountRepository.findById(sourceAccount.iban) >> Optional.of(sourceAccount)
        service.accountRepository.findById(AccountTestData.IBAN_2.value) >> Optional.of(Stub(Account))

        when:
        def result = service.transfer(new Iban(sourceAccount.iban), AccountTestData.IBAN_2, AccountTestData.MONEY)

        then:
        !result
    }

    def "transfer() from SAVINGS to referenced CHECKING is possible"() {
        given: 'a checking account, ...'
        def accountChecking = new Account(CHECKING, AccountTestData.IBAN_CHECKING) // account starts with 0.0 balance
        accountChecking.deposit(AccountTestData.MONEY)
        service.accountRepository.findById(accountChecking.iban) >> Optional.of(accountChecking)

        and: '... a savings account that belongs to it.'
        def accountSavings = new Account(AccountTestData.IBAN_SAVINGS, accountChecking)
        service.accountRepository.findById(accountSavings.iban) >> Optional.of(accountSavings)

        when:
        def result = service.transfer(new Iban(accountChecking.iban), new Iban(accountSavings.iban), AccountTestData.MONEY)

        then:
        result
    }

    def "transfer() from SAVINGS to any NON referenced account is NOT possible"() {
        given: 'a savings account of some account'
        def accountSavings = new Account(AccountTestData.IBAN_SAVINGS, Stub(Account)) // is associated with some account
        service.accountRepository.findById(accountSavings.iban) >> Optional.of(accountSavings)

        and: 'a checking account'
        def accountChecking = new Account(CHECKING, AccountTestData.IBAN_CHECKING)
        service.accountRepository.findById(AccountTestData.IBAN_CHECKING.value) >> Optional.of(accountChecking)

        when:
        def result = service.transfer(new Iban(accountSavings.iban), AccountTestData.IBAN_CHECKING, AccountTestData.MONEY)

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
        def accountPrivateLoan = new Account(PRIVATE_LOAN, AccountTestData.IBAN_LOAN) // account starts with 0.0 balance
        accountPrivateLoan.deposit(AccountTestData.MONEY)
        service.accountRepository.findById(accountPrivateLoan.iban) >> Optional.of(accountPrivateLoan)

        and: '... any other account.'
        def someAccount = new Account(AccountTestData.IBAN, accountPrivateLoan)
        service.accountRepository.findById(someAccount.iban) >> Optional.of(someAccount)

        when:
        def result = service.transfer(new Iban(accountPrivateLoan.iban), new Iban(someAccount.iban), AccountTestData.MONEY)

        then:
        !result

        where:
        destinationAccount << AccountType.values()
    }

    def "create() Creates an account with the given type "() {
        when:
        service.create(AccountTestData.ACCOUNT_TYPE, null)

        then:
        1 * service.accountRepository.save({ Account a ->
            a.accountType == AccountTestData.ACCOUNT_TYPE
            a.balance == 0.0
            a.iban =~ /DE[0-9]{20}/
        }) >> Stub(Account)
    }

    def "create() Savings account references checking "() {
        given:
        def accountType = AccountType.SAVINGS
        def checkingAccount = Stub(Account)

        when:
        service.create(accountType, AccountTestData.IBAN)

        then:
        1 * service.accountRepository.getOne(AccountTestData.IBAN.value) >> checkingAccount
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
        def result = service.getBalance(AccountTestData.IBAN)

        then:
        service.accountRepository.findById(AccountTestData.IBAN.value) >> Optional.of(account)

        result
                .map(m -> account.balance == m.amount)
                .orElse(false)
    }

    def 'getAll() will filter by account type'() {
        given:
        def filter = [AccountTestData.ACCOUNT_TYPE]
        def accounts = [Stub(Account)]

        when:
        def result = service.getAllFiltered(filter)

        then:
        1 * service.accountRepository.findByAccountTypeIn(filter) >> accounts
        result == accounts
    }

    def 'lock() will lock an account'() {
        when:
        service.lock(AccountTestData.IBAN)

        then:
        1 * service.accountRepository.findById(AccountTestData.IBAN.value) >> Optional.of(new Account())
        1 * service.accountRepository.save({ Account it -> it.locked })
    }

    def 'unlock() will unlock an account'() {
        when:
        service.unlock(AccountTestData.IBAN)

        then:
        1 * service.accountRepository.findById(AccountTestData.IBAN.value) >> Optional.of(new Account(locked: true))
        1 * service.accountRepository.save({ Account it -> !it.locked })
    }
}
