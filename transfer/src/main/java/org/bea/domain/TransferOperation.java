package org.bea.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "operation", schema = "transfer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferOperation {
    @Id
    private UUID id;

    private String fromUser;
    private String toUser;

    private String fromCurrency;
    private String toCurrency;

    private BigDecimal amount;
    private BigDecimal convertedAmount;

    private String status;        // OK / BLOCKED / ERROR
    private String blockerReason; // text

    private OffsetDateTime ts;
}
