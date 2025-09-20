package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.config.SharedAppProperties;
import org.bea.domain.CurrencyRate;
import org.bea.domain.Store;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
public class RatesController {

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        return new ArrayList<>(Store.currencyRates.values());
    }
}
