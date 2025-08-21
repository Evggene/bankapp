package org.bea.scheduler;

import org.bea.domain.Store;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class Scheduler {

    private final Random random = new Random();

    @Scheduled(fixedRate = 1000)
    public void updateRates() {
        Store.currencyRates.values().forEach(rate -> {
            double change = 1.0 + (random.nextDouble() * 0.1 - 0.05); // -5% до +5%
            var newValue = rate.getValue().multiply(BigDecimal.valueOf(change))
                    .setScale(2, RoundingMode.HALF_UP);
            rate.setValue(newValue);
        });
    }
}
