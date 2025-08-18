package org.bea.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BlockRequest {
    private String login;
    private String action;
    private BigDecimal value;
    private String currency;
}
