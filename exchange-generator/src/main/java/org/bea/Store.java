package org.bea;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    public static final ConcurrentHashMap<String, CurrencyRate> currencyRates = new ConcurrentHashMap<>()
    {{
        put("RUB", new CurrencyRate("Рубль", "RUB", BigDecimal.valueOf(1.0)));
        put("USD", new CurrencyRate("Доллар США", "USD", BigDecimal.valueOf(77.0)));
        put("CNY", new CurrencyRate("Китайский юань", "CNY", BigDecimal.valueOf(12.0)));
    }};
}
