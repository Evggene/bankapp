package org.bea.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account", schema = "cash",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "currency"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashAccount {

    @Id
    private UUID id;

    private String username;

    private String currency;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (balance == null) balance = BigDecimal.ZERO;
    }
}
