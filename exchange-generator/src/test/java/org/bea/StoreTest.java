package org.bea;

import org.bea.domain.Store;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StoreTest {

    @Test
    void initialCurrencies_present() {
        assertThat(Store.currencyRates).containsKeys("RUB","USD","CNY");
    }
}
