package org.bea.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferFormRequest {
    @NotBlank
    private String from_currency;
    @NotBlank
    private String to_currency;
    @NotNull @DecimalMin("0.01")
    private BigDecimal value;
    @NotBlank
    private String to_login;
}
