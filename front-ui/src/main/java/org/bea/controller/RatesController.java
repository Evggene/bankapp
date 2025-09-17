package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.config.SharedAppProperties;
import org.bea.domain.CurrencyRate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
public class RatesController {

    private final Supplier<String> clientCredentialsToken;
    private final RestTemplate restTemplate;
    private final SharedAppProperties properties;

    @GetMapping("/getRates")
    public Collection<CurrencyRate> getRates() {
        String token = clientCredentialsToken.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List<CurrencyRate>> response = restTemplate.exchange(
                properties.getGatewayBaseUrl() + "/exchange-generator/getRates",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {});
        return response.getBody();
    }
}
