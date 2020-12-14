package com.mruhwedel.banking

import spock.lang.Specification
import spock.lang.Unroll

import static com.mruhwedel.banking.AccountType.CHECKING
import static com.mruhwedel.banking.AccountType.PRIVATE_LOAN
import static com.mruhwedel.banking.BankingTestData.*
import static com.mruhwedel.banking.TransferResult.TRANSFERRED
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

    @Unroll
    def 'transfer() Checking to #destination is possible'(AccountType destination) {
        given: 'accounts'
        def sourceAccount = new Account(CHECKING, IBAN) // account starts with 0.0 balance
        sourceAccount.deposit(MONEY)
        service.accountRepository.findByIban(sourceAccount.iban.value) >> Optional.of(sourceAccount)

        def destinationAccount = new Account(destination, IBAN_2)
        service.accountRepository.findByIban(destinationAccount.iban.value) >> Optional.of(destinationAccount)

        when:
        def result = service.transfer(sourceAccount.iban, destinationAccount.iban, MONEY)

        then:
        result

        where:
        destination <<  AccountType.values()
    }

    def "transfer() from SAVINGS to referenced CHECKING is possible"() {
        given: 'a checking account, ...'
        def accountChecking = new Account(CHECKING, IBAN_CHECKING) // account starts with 0.0 balance
        accountChecking.deposit(MONEY)
        service.accountRepository.findByIban(accountChecking.iban.value) >> Optional.of(accountChecking)

        and: '... a savings account that belongs to it.'
        def accountSavings = new Account(IBAN_SAVINGS, accountChecking)
        service.accountRepository.findByIban(accountSavings.iban.value) >> Optional.of(accountSavings)

        when:
        def result = service.transfer(accountChecking.iban, accountSavings.iban, MONEY)

        then:
        result
    }

    def "transfer() from SAVINGS to any NON referenced account is NOT possible"() {
        given: 'a savings account of some account'
        def accountSavings = new Account(IBAN_SAVINGS, Stub(Account)) // is associated with some account
        service.accountRepository.findByIban(accountSavings.iban.value) >> Optional.of(accountSavings)

        and: 'a checking account'
        def accountChecking = new Account(CHECKING, IBAN_CHECKING)
        service.accountRepository.findByIban(IBAN_CHECKING.value) >> Optional.of(accountChecking)

        when:
        def result = service.transfer(accountSavings.iban, IBAN_CHECKING, MONEY)

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
        service.accountRepository.findByIban(accountPrivateLoan.iban.value) >> Optional.of(accountPrivateLoan)

        and: '... any other account.'
        def someAccount = new Account(IBAN, accountPrivateLoan)
        service.accountRepository.findByIban(someAccount.iban.value) >> Optional.of(someAccount)

        when:
        def result = service.transfer(accountPrivateLoan.iban, someAccount.iban, MONEY)

        then:
        !result

        where:
        destinationAccount << AccountType.values()
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
        }) >> Stub(Account)
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
        def accounts = [Stub(Account)]

        when:
        def result = service.getAllFiltered(filter)

        then:
        1 * service.accountRepository.findByAccountType(filter) >> accounts
        result == accounts
    }
}
