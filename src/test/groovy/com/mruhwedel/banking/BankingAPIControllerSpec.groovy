package com.mruhwedel.banking

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@SpringBootTest
class BankingAPIControllerSpec extends Specification {

   def  client = WebTestClient.bindToController(new BankingAPIController()).build();

    def 'deposits'(){
        expect:
        true
//        client.post().uri('/api/account/')

    }
}