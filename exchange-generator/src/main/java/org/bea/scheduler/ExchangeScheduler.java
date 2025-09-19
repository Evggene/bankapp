package org.bea.scheduler;

import lombok.RequiredArgsConstructor;
import org.bea.domain.Store;
import org.bea.service.ExchangeProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ExchangeScheduler {

    private final Random random = new Random();

    private final ExchangeProducer exchangeProducer;

    @Scheduled(fixedRate = 1000)
    public void updateRates() {
        Store.currencyRates.values().forEach(rate -> {
            double change = 1.0 + (random.nextDouble() * 0.1 - 0.05); // -5% до +5%
            var newValue = rate.getValue().multiply(BigDecimal.valueOf(change))
                    .setScale(2, RoundingMode.HALF_UP);
            rate.setValue(newValue);
        });
        exchangeProducer.sendRatesBatch(Store.currencyRates.values());
    }
}
