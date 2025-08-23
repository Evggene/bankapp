package org.bea.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashFormRequest {
    @NotBlank
    private String currency;
    @NotNull @DecimalMin("0.01")
    private BigDecimal value;
    @NotBlank
    private String action;
}
