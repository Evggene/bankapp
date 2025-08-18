package org.bea.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashFormRequest {
    @NotBlank
    private String currency;   // from <select name="currency">
    @NotNull @DecimalMin("0.01")
    private BigDecimal value;  // from <input name="value" type="number">
    @NotBlank
    private String action;     // from <button name="action" value="PUT|GET">
}
