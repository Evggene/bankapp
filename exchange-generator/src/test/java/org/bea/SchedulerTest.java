package org.bea;

import org.bea.domain.Store;
import org.bea.scheduler.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
    }

    @Test
    void updateRates_changesWithinFivePercentAndScale2() {
        // снимем слепок значений
        Map<String, BigDecimal> before = Store.currencyRates.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getValue()
                ));

        scheduler.updateRates();

        before.forEach((code, oldVal) -> {
            BigDecimal newVal = Store.currencyRates.get(code).getValue();
            // scale >= 2 (после setScale(2))
            assertThat(newVal.scale()).isGreaterThanOrEqualTo(2);
            // отношение в пределах [0.95; 1.05]
            double ratio = newVal.doubleValue() / oldVal.doubleValue();
            assertThat(ratio).isBetween(0.95, 1.05);
        });
    }
}
