package com.mruhwedel.banking;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Amount {
    private BigDecimal amount;
}
