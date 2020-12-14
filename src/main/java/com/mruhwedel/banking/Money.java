package com.mruhwedel.banking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {
     private BigDecimal amount;
}
