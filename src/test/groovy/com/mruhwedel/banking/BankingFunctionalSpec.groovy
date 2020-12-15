package com.mruhwedel.banking

import com.mruhwedel.banking.account.Account
import com.mruhwedel.banking.account.AccountType
import com.mruhwedel.banking.account.Iban
import com.mruhwedel.banking.account.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static com.mruhwedel.banking.account.AccountType.CHECKING
import static com.mruhwedel.banking.account.AccountType.PRIVATE_LOAN
import static com.mruhwedel.banking.account.AccountType.SAVINGS
import static com.mruhwedel.banking.account.AccountTestData.*
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Stepwise
@ActiveProfiles("Test")
// Dev profile would bring the prerequisite data with it
@SpringBootTest(webEnvironment = RANDOM_PORT)
class BankingFunctionalSpec extends Specification {


    @Autowired
    WebTestClient client


    @Shared
    Iban checkingIban

    def 'create an account'() {
        when:
        def exchange = createAccount(CHECKING)

        then:
        exchange.expectStatus().isCreated()
        exchange.expectBody(Iban)
                .value(this::setCheckingIban)
    }

    private WebTestClient.ResponseSpec createAccount(AccountType at, Iban ref = null) {
        client.post()
                .uri("/api/accounts")
                .bodyValue(new BankingAPIController.AccountCreationDto(at, ref))
                .exchange()
    }

    def 'deposit() allows depositions'() {
        when:
        def ex = client.post()
                .uri('/api/accounts/{iban}/deposit', checkingIban.value)
                .bodyValue(MONEY)
                .exchange()


        then: 'deposits'
        ex.expectStatus().isOk()
    }

    def '...getting the the balance will show the new balance'() {
        when:
        def exchange = client
                .get()
                .uri('/api/accounts/{iban}', checkingIban.value)
                .exchange()

        then:
        exchange
                .expectBody(Money)
                .value(equalTo(MONEY))
    }
    @Shared
    Iban savings

    def '...create savings account '() {
        when:
        def account = createAccount(SAVINGS, checkingIban)

        then:
        account.expectStatus()
                .isCreated()

                .expectBody(Iban)
                .value(this::setSavings)
    }

    @Shared
    BigDecimal transfer = 10.00

    def 'transfers from checking to savings '() {
        when:
        def exchange = client.post().uri('/api/accounts/{from}/transfer-to/{to}',
                checkingIban.value, savings.value)
                .bodyValue(new Money(transfer))
                .exchange()

        then:
        exchange.expectStatus().isOk()
    }


    def '... checking account will have a DEcreased balance '() {
        when:
        def exchange = client.get().uri('/api/accounts/{iban}', checkingIban.value)
                .exchange()

        then:
        exchange.expectBody(Money)
                .value(Money::getAmount, equalTo(MONEY.amount - transfer))
    }

    def '... savings account will have an INcreased  balance '() {
        when:
        def exchange = client.get().uri('/api/accounts/{iban}', savings.value)
                .exchange()

        then:
        exchange.expectBody(Money)
                .value(Money::getAmount, equalTo(transfer))
    }

    def 'Creating a loan account'() {
        when:
        def exchange = createAccount(PRIVATE_LOAN)

        then:
        exchange.expectStatus().isCreated()
    }

    def 'Querying by account types works'(Set<AccountType> types) {
        when:
        def exchange = client.get().uri(
                uriBuilder -> uriBuilder.path('/api/accounts')
                        .queryParam('accountTypes', types)
                        .build()
        )
                .exchange()
        then:
        exchange
                .expectBody(Collection<Account>).value(hasSize(types.size()))

        where:
        types << [
                EnumSet.allOf(AccountType),
                EnumSet.of(CHECKING, PRIVATE_LOAN),
                EnumSet.of(CHECKING)
        ]
    }

    def 'Locking an account works'() {
        when:
        def exchange = client.put()
                .uri('/api/accounts/{iban}/lock', savings.value)
                .exchange()

        then:
        exchange.expectStatus().isOk()
    }

    def 'UNlocking an account works'() {
        when:
        def exchange = client.delete()
                .uri('/api/accounts/{iban}/lock', savings.value)
                .exchange()

        then:
        exchange.expectStatus().isOk()
    }
}