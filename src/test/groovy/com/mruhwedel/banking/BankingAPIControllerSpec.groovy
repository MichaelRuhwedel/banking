package com.mruhwedel.banking


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo

@SpringBootTest
class BankingAPIControllerSpec extends Specification {

    def client = WebTestClient.bindToController(new BankingAPIController()).build();

    def 'deposits()'() {
        given:


        expect:
        client.post()
                .uri("/api/account/{selected}/deposit", BankingTestData.IBAN.iban)
    }

    def 'deposit() should up the balance'(){
        given:
        def accountResource = '/api/account/{selected}'

        when: 'deposits'
        client.post()
                .uri("$accountResource/deposit", BankingTestData.IBAN.iban)
                .bodyValue(BankingTestData.AMOUNT)


        and: 'get the balance'
        def exchange = client
                .get()
                .uri(accountResource, BankingTestData.IBAN.iban)
                .exchange()

        then:
        exchange
                .expectBody(Amount)
                .value(equalTo(BankingTestData.AMOUNT))
    }

    def 'transfers() '() {
        expect:
        client.post().uri('/api/account/')
    }

}