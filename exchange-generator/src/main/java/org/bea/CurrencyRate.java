package org.bea;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyRate {
    private String title;
    private String name;
    private BigDecimal value;
}
