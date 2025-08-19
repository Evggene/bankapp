package org.bea.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversion_operation", schema = "exchange")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ConversionOperation {
    @Id
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime ts;

    @Column(nullable = false, length = 10)
    private String action;

    @Column(name = "from_currency", nullable = false, length = 10)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 10)
    private String toCurrency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "rate_from_rub", nullable = false, precision = 19, scale = 6)
    private BigDecimal rateFromRub;

    @Column(name = "rate_to_rub", nullable = false, precision = 19, scale = 6)
    private BigDecimal rateToRub;

    @Column(name = "conversion_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionRate;

    @Column(name = "result_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal resultAmount;
}
