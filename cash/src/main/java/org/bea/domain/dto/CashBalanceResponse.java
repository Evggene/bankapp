package org.bea.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CashBalanceResponse {
    private String login;
    private String currency;
    private BigDecimal balance;
    private String message;
}
