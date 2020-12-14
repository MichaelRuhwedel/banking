package com.mruhwedel.banking

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static com.mruhwedel.banking.AccountType.CHECKING
import static com.mruhwedel.banking.AccountType.SAVINGS
import static com.mruhwedel.banking.BankingTestData.*
import static org.hamcrest.Matchers.equalTo
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Stepwise
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
                .bodyValue(new AccountCreationDto(at, ref))
                .exchange()
    }


    def 'deposit() allows depositions'() {
        when:
        def ex = client.post()
                .uri('/api/accounts/{selected}/deposit', checkingIban.value)
                .bodyValue(MONEY)
                .exchange()


        then: 'deposits'
        ex.expectStatus().isOk()
    }

    def '...getting the the balance will show the new balance'() {
        when:
        def exchange = client
                .get()
                .uri('/api/accounts/{selected}', checkingIban.value)
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


    def '... checking account will have an DEcreased  balance '() {
        when:
        def exchange = client.get().uri('/api/accounts/{selected}', checkingIban.value)
                .exchange()

        then:
        exchange.expectBody(Money)
                .value(Money::getAmount, equalTo(MONEY.amount - transfer))
    }

    def '... savings account will have an INcreased  balance '() {
        when:
        def exchange = client.get().uri('/api/accounts/{selected}', savings.value)
                .exchange()

        then:
        exchange.expectBody(Money)
                .value(Money::getAmount, equalTo(transfer))
    }
}