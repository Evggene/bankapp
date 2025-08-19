package org.bea.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RateClient {

    @Autowired
    private RestTemplate restTemplate;

    @Data
    public static class CurrencyRate {
        private String title;
        private String name;
        private BigDecimal value;
    }

    public Map<String, BigDecimal> getRatesMap() {
        var url = "http://gateway/exchange-generator/getRates";
        var resp = restTemplate.getForObject(url, CurrencyRate[].class);
        if (resp == null) return Map.of();

        Collection<CurrencyRate> list = java.util.List.of(resp);
        return list.stream().collect(Collectors.toMap(CurrencyRate::getName, CurrencyRate::getValue));
    }
}
