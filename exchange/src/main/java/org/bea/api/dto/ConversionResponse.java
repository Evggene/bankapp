package org.bea.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ConversionResponse {
    private UUID id;
    private String action;
    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal rateFromRub;
    private BigDecimal rateToRub;
    private BigDecimal conversionRate;
    private BigDecimal resultAmount;
}
