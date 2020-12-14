package com.mruhwedel.banking;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

import static com.mruhwedel.banking.AccountType.*;

@Slf4j
@SpringBootApplication
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    @Bean
    @Profile("Dev")
    public CommandLineRunner demo(AccountService accountService) {
        return (args) -> {
            log.info("Setting up Prerequisites");
            Iban checking = accountService.create(CHECKING, null);
            accountService.create(SAVINGS, checking);
            accountService.create(PRIVATE_LOAN, null);

            // fetch all customers
            log.info("Accounts found with findAll():");
            log.info("-------------------------------");
            for (Account account : accountService.getAllFiltered(Arrays.asList(AccountType.values()))) {
                log.info("{}: {} references: {}", account.getAccountType(), account.getIban(), account.getChecking());
            }
        };
    }
}