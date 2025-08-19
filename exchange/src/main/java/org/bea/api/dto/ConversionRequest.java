package org.bea.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ConversionRequest {
    private String action;
    private String from;
    private String to;
    private BigDecimal amount;
}
