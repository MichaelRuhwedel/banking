package com.mruhwedel.banking.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@Valid
@NoArgsConstructor
@AllArgsConstructor
public class Money {
     @Min(0)
     private BigDecimal amount;
}
