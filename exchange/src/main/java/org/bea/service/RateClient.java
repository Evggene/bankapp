package org.bea.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bea.config.SharedAppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RateClient {

    private final RestTemplate restTemplate;
    private final SharedAppProperties properties;

    @Data
    public static class CurrencyRate {
        private String title;
        private String name;
        private BigDecimal value;
    }

    public Map<String, BigDecimal> getRatesMap() {
        var url = properties.getGatewayBaseUrl() + "/exchange-generator/getRates";
        var resp = restTemplate.getForObject(url, CurrencyRate[].class);
        if (resp == null) return Map.of();

        Collection<CurrencyRate> list = java.util.List.of(resp);
        return list.stream().collect(Collectors.toMap(CurrencyRate::getName, CurrencyRate::getValue));
    }
}
