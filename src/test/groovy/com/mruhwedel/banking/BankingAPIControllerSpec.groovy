package com.mruhwedel.banking


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

import static com.mruhwedel.banking.BankingTestData.*

import static org.hamcrest.Matchers.equalTo

@SpringBootTest
class BankingAPIControllerSpec extends Specification {

    def client = WebTestClient.bindToController(new BankingAPIController()).build();

    def 'deposits()'() {
        expect:
        client.post()
                .uri("/api/account/{selected}/deposit", IBAN.value)
    }

    def 'deposit() should up the balance when a deposit has been made'(){

        when: 'deposits'
        client.post()
                .uri('/api/account/{selected}/deposit', IBAN.value)
                .bodyValue(MONEY)


        and: 'get the balance'
        def exchange = client
                .get()
                .uri('/api/account/{selected}', IBAN.value)
                .exchange()

        then:
        exchange
                .expectBody(Money)
                .value(equalTo(MONEY))
    }

    def 'transfers() '() {
        expect:
        client.post().uri('/api/account/')
    }

}