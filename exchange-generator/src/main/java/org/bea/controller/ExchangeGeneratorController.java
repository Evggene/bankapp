package org.bea.controller;

import org.bea.domain.CurrencyRate;
import org.bea.domain.Store;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
public class ExchangeGeneratorController {

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        return Store.currencyRates.values();
    }
}
