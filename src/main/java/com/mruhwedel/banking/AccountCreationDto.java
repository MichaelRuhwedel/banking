package com.mruhwedel.banking;

import lombok.Data;
import lombok.Value;

@Value
public class AccountCreationDto {
    AccountType accountType;
    Iban referenceIban;
}
