package com.mruhwedel.banking

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class BankingApplicationSpec extends Specification {

    def 'loads'(){
        expect:
        true // we wouldn't get here if all crashes
    }
}