package org.bea.domain;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    public static final ConcurrentHashMap<String, CurrencyRate> currencyRates = new ConcurrentHashMap<>();
}
