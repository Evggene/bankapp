package org.bea.controller;

import org.bea.domain.CurrencyRate;
import org.bea.domain.Store;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class ExchangeGeneratorController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        return Store.currencyRates.values();
    }
}
